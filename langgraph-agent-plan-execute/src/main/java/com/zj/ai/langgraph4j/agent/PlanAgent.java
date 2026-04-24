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
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 计划制定 Agent
 * 分析用户问题，生成可执行的计划步骤
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Component
public class PlanAgent implements NodeAction<PlanExecuteState> {

    private final DynamicModelManager modelManager;
    private final ToolRegistryService toolRegistry;

    public PlanAgent(DynamicModelManager modelManager, ToolRegistryService toolRegistry) {
        this.modelManager = modelManager;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public Map<String, Object> apply(PlanExecuteState state) throws Exception {
        log.info("=== PlanAgent: 开始制定计划 ===");
        log.info("用户问题: {}", state.getUserQuery());

        // 1. 获取可用工具列表
        List<ToolConfigEntity> tools = toolRegistry.getEnabledTools();
        String toolDescriptions = buildToolDescriptions(tools);

        // 2. 构建计划生成 Prompt
        String prompt = buildPlanPrompt(state.getUserQuery(), toolDescriptions);

        // 3. 调用模型生成计划
        ChatModel model = modelManager.getDefaultChatModel();
        String planResponse = model.chat(prompt);

        log.info("模型返回的计划:\n{}", planResponse);
        // 4. 解析计划步骤
        String finalAnswer = JSONUtils.getJsonValue(planResponse, "finalAnswer");
        Object stepsObj = JSONUtils.getJsonValue(planResponse, "steps");

        List<PlanStep> steps = new ArrayList<>();
        if (stepsObj instanceof List<?> stepsList && !stepsList.isEmpty()) {
            // 将 LinkedHashMap 转换为 PlanStep
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

        if (CollectionUtils.isEmpty(steps) && Objects.nonNull(finalAnswer)) {
            state.setFinalAnswer(finalAnswer);
            state.setPlanFeasible(false);
            state.setCompleted(true);
            return state.toMap();
        }
        if (!CollectionUtils.isEmpty(steps)) {
            // 5. 更新状态
            state.setPlanSteps(steps);
            log.info("生成的计划步骤数: {}", steps.size());
            for (PlanStep step : steps) {
                log.info("  步骤 {}: {} - 工具: {}", step.getStepIndex(), step.getDescription(), step.getToolName());
            }
            return state.toMap();
        }
        // steps 为空，finalAnswer 为空
        state.setFinalAnswer("计划生成失败，请检查模型配置");
        state.setPlanSteps(Collections.emptyList());
        state.setCompleted(true);
        return state.toMap();
    }

    /**
     * 构建工具描述
     */
    private String buildToolDescriptions(List<ToolConfigEntity> tools) {
        StringBuilder sb = new StringBuilder();
        for (ToolConfigEntity tool : tools) {
            sb.append(String.format("- %s: %s\n", tool.getToolName(),
                    tool.getDescription() != null ? tool.getDescription() : "无描述"));
        }
        return sb.toString();
    }

    /**
     * 构建计划生成 Prompt
     */
    private String buildPlanPrompt(String userQuery, String toolDescriptions) {
        return """
                你是一个智能助手的计划制定模块。请分析用户的问题，并制定一个可执行的计划。

                用户问题: %s

                可用工具:
                %s

                请按照以下 JSON 格式输出计划（只输出 JSON，不要有其他内容）:
                {
                  "steps": [
                    {
                      "stepIndex": 1,
                      "description": "步骤描述",
                      "toolName": "工具名称",
                      "toolInput": "工具输入参数"
                    }
                  ],
                  "finalAnswer": "预期的最终答案描述"
                }

                要求:
                1. 每个步骤必须使用一个可用工具
                2. 步骤之间要有逻辑顺序
                3. toolInput 应该是具体的参数值，不是描述
                4. 如果用户问题不需要使用工具，直接在 finalAnswer 中给出答案

                请输出计划:
                """.formatted(userQuery, toolDescriptions);
    }
}
