package com.zj.ai.langgraph4j.agent;

import com.zj.ai.common.sdk.json.JSONUtils;
import com.zj.ai.langgraph4j.domain.constants.StepStatus;
import com.zj.ai.langgraph4j.domain.dto.PlanStep;
import com.zj.ai.langgraph4j.domain.entity.ToolConfigEntity;
import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import com.zj.ai.langgraph4j.service.DynamicModelManager;
import com.zj.ai.langgraph4j.service.ToolRegistryService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 重新规划 Agent
 * 当计划不可行或执行失败时，生成新的计划
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Component
public class ReplanAgent implements NodeAction<PlanExecuteState> {

    private final DynamicModelManager modelManager;
    private final ToolRegistryService toolRegistry;

    public ReplanAgent(DynamicModelManager modelManager, ToolRegistryService toolRegistry) {
        this.modelManager = modelManager;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public Map<String, Object> apply(PlanExecuteState state) throws Exception {
        log.info("=== ReplanAgent: 开始重新规划 ===");
        log.info("当前重规划次数: {}/{}, 连续验证失败: {}, 连续执行失败: {}",
                state.getRePlanCount(), state.getMaxRePlanAttempts(),
                state.getConsecutiveValidationFailures(), state.getConsecutiveExecutionFailures());

        // 1. 增加重规划计数
        int newReplanCount = state.getRePlanCount() + 1;
        state.setRePlanCount(newReplanCount);

        // 2. 检查是否达到最大失败阈值
        if (state.hasExceededMaxFailures()) {
            log.warn("已达到最大失败阈值，终止重规划");
            state.setErrorMessage("多次尝试后仍无法生成有效计划，请简化您的问题或稍后重试");
            state.setCompleted(true);
            return state.toMap();
        }

        // 3. 构建包含失败信息的 Prompt
        String prompt = buildReplanPrompt(state);

        // 4. 调用模型生成新计划
        ChatModel model = modelManager.getDefaultChatModel();
        String planResponse = model.chat(prompt);

        log.info("模型返回的新计划:\n{}", planResponse);

        // 5. 解析新计划
        List<PlanStep> newSteps = parsePlanSteps(planResponse);

        // 6. 检查新计划是否为空
        if (newSteps.isEmpty()) {
            log.warn("新计划为空，尝试给出直接回答");
            String finalAnswer = JSONUtils.getJsonValue(planResponse, "finalAnswer");
            if (finalAnswer != null && !finalAnswer.isEmpty()) {
                state.setFinalAnswer(finalAnswer);
                state.setCompleted(true);
            } else {
                state.setValidationResult("重规划失败：无法生成有效计划");
                state.setPlanFeasible(false);
            }
            return state.toMap();
        }

        // 7. 更新状态
        state.setPlanSteps(newSteps);
        state.setNeedReplan(false);
        // 不设置 planFeasible，让 ValidateAgent 来判断

        log.info("新计划步骤数: {}", newSteps.size());
        for (PlanStep step : newSteps) {
            log.info("  步骤 {}: {} - 工具: {}", step.getStepIndex(), step.getDescription(), step.getToolName());
        }

        return state.toMap();
    }

    /**
     * 构建重新规划 Prompt
     */
    private String buildReplanPrompt(PlanExecuteState state) {
        // 获取可用工具
        List<ToolConfigEntity> tools = toolRegistry.getEnabledTools();
        StringBuilder toolDesc = new StringBuilder();
        for (ToolConfigEntity tool : tools) {
            toolDesc.append(String.format("- %s: %s\n", tool.getToolName(),
                    tool.getDescription() != null ? tool.getDescription() : "无描述"));
        }

        // 构建原计划描述
        StringBuilder originalPlan = new StringBuilder();
        if (state.hasPlan()) {
            for (PlanStep step : state.getPlanSteps()) {
                originalPlan.append(String.format("%d. %s (工具: %s, 输入: %s)\n",
                        step.getStepIndex(), step.getDescription(),
                        step.getToolName(), step.getToolInput()));
            }
        }

        // 构建执行结果描述（如果有）
        StringBuilder executionResult = new StringBuilder();
        if (!state.getExecutionResults().isEmpty()) {
            executionResult.append("之前的执行结果:\n");
            state.getExecutionResults().forEach(result -> {
                if (result.isSuccess()) {
                    executionResult.append(String.format("- 步骤 %d (%s): 成功 - %s\n",
                            result.getStepIndex(), result.getToolName(), result.getResult()));
                } else {
                    executionResult.append(String.format("- 步骤 %d (%s): 失败 - %s\n",
                            result.getStepIndex(), result.getToolName(), result.getErrorMessage()));
                }
            });
        }

        String failureContext = buildFailureContext(state);

        return """
                你是一个智能助手的计划制定模块。之前的计划有问题，需要重新制定计划。

                用户问题: %s

                之前的计划:
                %s

                %s

                失败原因:
                %s

                可用工具:
                %s

                %s

                请分析失败原因，制定一个新的、可行的计划。

                请按照以下 JSON 格式输出新计划（只输出 JSON，不要有其他内容）:
                {
                  "steps": [
                    {
                      "stepIndex": 1,
                      "description": "步骤描述",
                      "toolName": "工具名称",
                      "toolInput": "具体的工具输入参数"
                    }
                  ],
                  "finalAnswer": "如果不需要使用工具，直接给出答案"
                }

                要求:
                1. 必须解决之前计划的问题
                2. 只使用可用工具列表中的工具
                3. 工具输入参数要具体，不能是描述
                4. 步骤要简洁有效，避免重复之前的错误
                5. 如果问题过于复杂，可以尝试简化解决方案
                6. 如果不需要使用工具，在 finalAnswer 中直接给出答案

                请输出新计划:
                """.formatted(
                state.getUserQuery(),
                originalPlan.length() > 0 ? originalPlan.toString() : "无",
                executionResult.length() > 0 ? executionResult.toString() : "",
                state.getValidationResult(),
                toolDesc.toString(),
                failureContext
        );
    }

    /**
     * 构建失败上下文提示
     */
    private String buildFailureContext(PlanExecuteState state) {
        StringBuilder context = new StringBuilder();

        if (state.getConsecutiveValidationFailures() > 0) {
            context.append(String.format("注意: 已经连续验证失败 %d 次，请尝试不同的方案。\n",
                    state.getConsecutiveValidationFailures()));
        }

        if (state.getConsecutiveExecutionFailures() > 0) {
            context.append(String.format("注意: 已经连续执行失败 %d 次，请考虑简化任务或使用不同的工具。\n",
                    state.getConsecutiveExecutionFailures()));
        }

        if (state.isSameAsLastPlan()) {
            context.append("重要: 新计划不能与之前的计划完全相同，必须有所改变。\n");
        }

        return context.toString();
    }

    /**
     * 解析计划步骤
     */
    private List<PlanStep> parsePlanSteps(String planResponse) {
        List<PlanStep> steps = new ArrayList<>();

        try {
            Object stepsObj = JSONUtils.getJsonValue(planResponse, "steps");
            if (stepsObj instanceof List<?> stepsList) {
                for (Object item : stepsList) {
                    if (item instanceof Map<?, ?> stepMap) {
                        int stepIndex = stepMap.get("stepIndex") instanceof Number n ? n.intValue() : steps.size() + 1;
                        String description = (String) stepMap.get("description");
                        String toolName = (String) stepMap.get("toolName");
                        String toolInput = (String) stepMap.get("toolInput");

                        steps.add(new PlanStep(stepIndex, description, toolName, toolInput, null, StepStatus.PENDING));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("JSON 解析失败: {}", e.getMessage());
        }

        return steps;
    }
}
