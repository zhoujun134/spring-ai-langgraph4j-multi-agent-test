package com.zj.ai.langgraph4j.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 当计划不可行时，生成新的计划
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Component
public class ReplanAgent implements NodeAction<PlanExecuteState> {

    private final DynamicModelManager modelManager;
    private final ToolRegistryService toolRegistry;
    private final ObjectMapper objectMapper;

    public ReplanAgent(DynamicModelManager modelManager, ToolRegistryService toolRegistry) {
        this.modelManager = modelManager;
        this.toolRegistry = toolRegistry;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Map<String, Object> apply(PlanExecuteState state) throws Exception {
        log.info("=== ReplanAgent: 开始重新规划 ===");
        log.info("当前重规划次数: {}/{}", state.getRePlanCount(), state.getMaxRePlanAttempts());

        // 1. 增加重规划计数
        int newReplanCount = state.getRePlanCount() + 1;
        state.setRePlanCount(newReplanCount);
        log.info("更新后重规划次数: {}/{}", newReplanCount, state.getMaxRePlanAttempts());

        // 2. 构建包含失败信息的 Prompt
        String prompt = buildReplanPrompt(state);

        // 3. 调用模型生成新计划
        ChatModel model = modelManager.getDefaultChatModel();
        String planResponse = model.chat(prompt);

        log.info("模型返回的新计划:\n{}", planResponse);

        // 4. 解析新计划
        List<PlanStep> newSteps = parsePlanSteps(planResponse);

        // 5. 更新状态
        state.setPlan(newSteps);
        state.setPlanFeasible(false); // 重置为 false，等待重新验证

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
            for (PlanStep step : state.getPlan()) {
                originalPlan.append(String.format("%d. %s (工具: %s)\n",
                        step.getStepIndex(), step.getDescription(), step.getToolName()));
            }
        }

        return """
                你是一个智能助手的计划制定模块。之前的计划验证失败了，需要重新制定计划。

                用户问题: %s

                之前的计划:
                %s

                验证失败原因:
                %s

                可用工具:
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
                  "improvementNote": "改进说明"
                }

                要求:
                1. 必须解决之前计划的问题
                2. 只使用可用工具列表中的工具
                3. 工具输入参数要具体，不能是描述
                4. 步骤要简洁有效

                请输出新计划:
                """.formatted(
                state.getUserQuery(),
                originalPlan.length() > 0 ? originalPlan.toString() : "无",
                state.getValidationResult(),
                toolDesc.toString()
        );
    }

    /**
     * 解析计划步骤
     */
    private List<PlanStep> parsePlanSteps(String planResponse) {
        List<PlanStep> steps = new ArrayList<>();

        try {
            // 提取 JSON 内容
            String jsonContent = extractJson(planResponse);

            if (jsonContent != null) {
                Map<String, Object> planMap = objectMapper.readValue(jsonContent, Map.class);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> stepsList = (List<Map<String, Object>>) planMap.get("steps");

                if (stepsList != null) {
                    for (Map<String, Object> stepMap : stepsList) {
                        int stepIndex = stepMap.get("stepIndex") != null ?
                                ((Number) stepMap.get("stepIndex")).intValue() : steps.size() + 1;
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

        // 如果解析失败，返回空列表
        return steps;
    }

    /**
     * 提取 JSON 内容
     */
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }
}
