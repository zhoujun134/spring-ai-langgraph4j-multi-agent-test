package com.zj.ai.study.service.agent;

import com.zj.ai.study.domain.dto.AnalysisTaskState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 任务拆解 Agent：解析用户需求中的关键信息（时间范围、指标、分析维度）
 */
@Component
public class TaskDecompositionAgent implements NodeAction<AnalysisTaskState> {

    private final ChatModel chatModel;

    // 注入 Spring AI 管理的 AI 模型
    public TaskDecompositionAgent(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    // 节点执行逻辑：输入状态，输出更新后的状态
    public AnalysisTaskState decompose(AnalysisTaskState state) {
        String userQuery = state.getUserQuery();
        // 构建提示词：让 AI 解析需求中的关键信息，返回结构化结果
        String prompt = String.format("""
                请解析用户的数据分析需求，提取以下3个关键信息，用 JSON 格式返回（仅返回 JSON，不要额外内容）：
                1. timeRange：时间范围（如 2025年Q3、2025年7-9月）
                2. targetIndicator：目标指标（如 销量、销售额、订单数）
                3. analysisDimension：分析维度（如 品类、区域、品牌）
                                
                用户需求：%s
                """, userQuery);
        System.out.println("任务拆解提示词：" + prompt);
        // 调用 AI 模型获取解析结果
        String jsonResult = chatModel.call(prompt);
        System.out.println("任务拆解结果：" + jsonResult);

        // 解析 JSON 到状态（简化处理，实际项目可用 Jackson 解析）
        return parseJsonToState(state, jsonResult);
    }

    // 简化 JSON 解析（实际项目建议用 Jackson 或 Gson）
    private AnalysisTaskState parseJsonToState(AnalysisTaskState state, String json) {
        String timeRange = extractValue(json, "timeRange");
        String targetIndicator = extractValue(json, "targetIndicator");
        String analysisDimension = extractValue(json, "analysisDimension");

        return state
                .withTimeRange(timeRange)
                .withTargetIndicator(targetIndicator)
                .withAnalysisDimension(analysisDimension);
    }

    private String extractValue(String json, String key) {
        String prefix = "\"" + key + "\":";
        int startIndex = json.indexOf(prefix) + prefix.length();
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = json.indexOf("}", startIndex);
        }
        return json.substring(startIndex).trim().replace("\"", "").replace("}", "").trim();
    }

    @Override
    public Map<String, Object> apply(AnalysisTaskState analysisTaskState) throws Exception {
        AnalysisTaskState result = this.decompose(analysisTaskState);
        return result.toMap();
    }
}
