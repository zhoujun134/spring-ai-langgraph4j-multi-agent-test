package com.zj.ai.study.service.agent;

import com.zj.ai.study.domain.dto.AnalysisTaskState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * 数据分析 Agent：对原始数据进行统计分析，提炼关键结论（如 Top3、增长率）
 */
@Component
public class DataAnalysisAgent implements NodeAction<AnalysisTaskState> {

    private final ChatModel chatModel;

    public DataAnalysisAgent(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public AnalysisTaskState analyze(AnalysisTaskState state) {
        String userQuery = state.getUserQuery();
        String rawData = state.getRawData();
        String targetIndicator = state.getTargetIndicator();
        String analysisDimension = state.getAnalysisDimension();

        // 构建提示词：让 AI 分析原始数据，生成结构化结论
        String prompt = String.format("""
                以下是原始数据（%s按%s统计）：
                %s
                
                请完成以下分析：
                1. 找出%s排名前3的%s；
                2. 计算前3名的总%s占比；
                3. 分析前3名领先的可能原因（结合电商行业常识）；
                4. 结论简洁明了，分点说明（不要超过500字）。
                
                用户原始需求：%s
                """, targetIndicator, analysisDimension, rawData, targetIndicator, analysisDimension, targetIndicator, userQuery);

        // 调用 AI 模型获取分析结果
        String analysisResult = chatModel.call(prompt);
        System.out.println("数据分析结论：" + analysisResult);

        return state.withAnalysisResult(analysisResult);
    }


    @Override
    public Map<String, Object> apply(AnalysisTaskState analysisTaskState) throws Exception {
        AnalysisTaskState result = this.analyze(analysisTaskState);
        return result.toMap();
    }
}
