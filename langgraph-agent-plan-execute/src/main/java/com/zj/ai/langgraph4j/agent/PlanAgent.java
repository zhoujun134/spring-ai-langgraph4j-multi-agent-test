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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final ObjectMapper objectMapper;

    public PlanAgent(DynamicModelManager modelManager, ToolRegistryService toolRegistry) {
        this.modelManager = modelManager;
        this.toolRegistry = toolRegistry;
        this.objectMapper = new ObjectMapper();
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
        List<PlanStep> steps = parsePlanSteps(planResponse);

        // 5. 更新状态
        state.setPlan(steps);
        // 注意：不要重置 replanCount，让它保持递增状态

        log.info("生成的计划步骤数: {}", steps.size());
        for (PlanStep step : steps) {
            log.info("  步骤 {}: {} - 工具: {}", step.getStepIndex(), step.getDescription(), step.getToolName());
        }

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

    /**
     * 解析计划步骤
     */
    private List<PlanStep> parsePlanSteps(String planResponse) {
        List<PlanStep> steps = new ArrayList<>();

        try {
            // 尝试提取 JSON 部分
            String jsonContent = extractJson(planResponse);

            if (jsonContent != null) {
                // 解析 JSON
                Map<String, Object> planMap = objectMapper.readValue(jsonContent, Map.class);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> stepsList = (List<Map<String, Object>>) planMap.get("steps");

                if (stepsList != null) {
                    for (Map<String, Object> stepMap : stepsList) {
                        int stepIndex = ((Number) stepMap.get("stepIndex")).intValue();
                        String description = (String) stepMap.get("description");
                        String toolName = (String) stepMap.get("toolName");
                        String toolInput = (String) stepMap.get("toolInput");

                        steps.add(new PlanStep(stepIndex, description, toolName, toolInput, null, StepStatus.PENDING));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("JSON 解析失败，尝试使用正则表达式解析: {}", e.getMessage());
            // 使用正则表达式解析
            steps = parseWithRegex(planResponse);
        }

        // 如果解析失败，创建一个默认步骤
        if (steps.isEmpty()) {
            log.warn("计划解析失败，创建默认搜索步骤");
            steps.add(PlanStep.pending(1, "搜索相关信息", "search", "默认搜索"));
        }

        return steps;
    }

    /**
     * 提取 JSON 内容
     */
    private String extractJson(String text) {
        // 尝试找到 JSON 块
        Pattern pattern = Pattern.compile("\\{[\\s\\S]*\\}", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 使用正则表达式解析
     */
    private List<PlanStep> parseWithRegex(String text) {
        List<PlanStep> steps = new ArrayList<>();

        // 简单的步骤解析
        Pattern stepPattern = Pattern.compile("步骤\\s*(\\d+)[:：]\\s*(.+?)(?=步骤|$)", Pattern.DOTALL);
        Matcher matcher = stepPattern.matcher(text);

        int index = 1;
        while (matcher.find()) {
            String description = matcher.group(2).trim();
            // 尝试提取工具名
            String toolName = extractToolName(description);
            steps.add(PlanStep.pending(index++, description, toolName, description));
        }

        return steps;
    }

    /**
     * 从描述中提取工具名
     */
    private String extractToolName(String description) {
        if (description.contains("计算") || description.contains("数学")) {
            return "calculator";
        } else if (description.contains("天气")) {
            return "weather";
        } else if (description.contains("搜索") || description.contains("查找")) {
            return "search";
        }
        return "search"; // 默认使用搜索
    }
}
