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

        // 4. 清理模型返回（去除 markdown 代码块格式）
        String cleanedResponse = cleanModelResponse(planResponse);

        // 5. 解析计划步骤和最终答案
        String finalAnswer = JSONUtils.getJsonValue(cleanedResponse, "finalAnswer");
        Object stepsObj = JSONUtils.getJsonValue(cleanedResponse, "steps");

        List<PlanStep> steps = new ArrayList<>();
        if (stepsObj instanceof List<?> stepsList && !stepsList.isEmpty()) {
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

        // 6. 决策逻辑：优先使用直接答案
        boolean hasDirectAnswer = isDirectAnswer(finalAnswer, steps);

        if (hasDirectAnswer) {
            // 模型已经直接给出了答案，无需执行工具
            log.info("模型直接给出了答案，无需执行工具");
            state.setFinalAnswer(finalAnswer);
            state.setPlanSteps(Collections.emptyList());
            state.setPlanFeasible(true);
            state.setCompleted(true);
            return state.toMap();
        }

        if (!CollectionUtils.isEmpty(steps)) {
            // 需要执行工具来获取答案
            state.setPlanSteps(steps);
            log.info("生成的计划步骤数: {}", steps.size());
            for (PlanStep step : steps) {
                log.info("  步骤 {}: {} - 工具: {}", step.getStepIndex(), step.getDescription(), step.getToolName());
            }
            return state.toMap();
        }

        // steps 为空，finalAnswer 也为空或无效
        if (Objects.nonNull(finalAnswer)) {
            state.setFinalAnswer(finalAnswer);
        } else {
            state.setFinalAnswer("计划生成失败，请检查模型配置");
        }
        state.setPlanSteps(Collections.emptyList());
        state.setCompleted(true);
        return state.toMap();
    }

    /**
     * 清理模型返回的响应（去除 markdown 代码块格式）
     */
    private String cleanModelResponse(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }

        String trimmed = response.trim();

        // 去除 ```json ... ``` 或 ``` ... ``` 格式
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7); // 去除 ```json
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3); // 去除 ```
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3); // 去除结尾的 ```
        }

        return trimmed.trim();
    }

    /**
     * 判断 finalAnswer 是否是直接答案（无需执行工具）
     *
     * 判断逻辑：
     * 1. finalAnswer 不为空
     * 2. finalAnswer 不是"预期答案描述"（不以"基于"、"根据"等词开头）
     * 3. finalAnswer 已经是一个完整的回答，长度足够
     */
    private boolean isDirectAnswer(String finalAnswer, List<PlanStep> steps) {
        if (finalAnswer == null || finalAnswer.trim().isEmpty()) {
            return false;
        }

        String trimmed = finalAnswer.trim();

        // 如果 finalAnswer 以这些关键词开头，说明它是"预期答案描述"，不是实际答案
        String[] descriptionPrefixes = {
            "基于", "根据", "通过", "利用", "借助", "参考",
            "预期", "将要", "将会", "能够"
        };

        for (String prefix : descriptionPrefixes) {
            if (trimmed.startsWith(prefix)) {
                return false;
            }
        }

        // 如果 finalAnswer 长度足够且没有步骤，说明是直接答案
        if (steps.isEmpty() && trimmed.length() > 20) {
            return true;
        }

        // 如果有步骤但 finalAnswer 明确表示不需要工具（例如"直接回答"）
        if (!steps.isEmpty() && trimmed.contains("直接") && trimmed.length() > 50) {
            return true;
        }

        return false;
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
                你是一个智能助手的计划制定模块。请分析用户的问题，决定是否需要使用工具来回答。

                用户问题: %s

                可用工具:
                %s

                请按照以下 JSON 格式输出（只输出 JSON，不要有其他内容）:

                情况1 - 如果用户问题可以直接回答（如知识问答、概念解释、对比分析等），无需使用工具：
                {
                  "steps": [],
                  "finalAnswer": "直接给出完整、详细的答案..."
                }

                情况2 - 如果需要使用工具才能回答（如查询天气、计算、搜索最新信息等）：
                {
                  "steps": [
                    {
                      "stepIndex": 1,
                      "description": "步骤描述",
                      "toolName": "工具名称",
                      "toolInput": "工具输入参数"
                    }
                  ],
                  "finalAnswer": ""
                }

                重要规则:
                1. 如果能用你的知识直接回答问题，就不要使用工具，直接在 finalAnswer 中给出完整答案
                2. 只有当问题需要实时数据或特定工具功能时，才生成 steps
                3. 每个步骤必须使用一个可用工具
                4. toolInput 应该是具体的参数值，不是描述
                5. 不要同时生成 steps 和非空的 finalAnswer

                请输出计划:
                """.formatted(userQuery, toolDescriptions);
    }
}
