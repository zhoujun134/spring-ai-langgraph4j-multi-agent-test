package com.zj.ai.study.service.agent;

import com.zj.ai.study.domain.dto.AnalysisTaskState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 报告生成 Agent：将分析结论整理为结构化报告
 */
@Component
public class ReportGenerationAgent implements NodeAction<AnalysisTaskState> {

    private final ChatModel chatModel;

    public ReportGenerationAgent(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public AnalysisTaskState generate(AnalysisTaskState state) {
        String userQuery = state.getUserQuery();
        String analysisResult = state.getAnalysisResult();
        String timeRange = state.getTimeRange();

        // 构建提示词：生成结构化报告
        String prompt = String.format("""
                请根据以下分析结论，生成一份正式的数据分析报告，格式要求：
                1. 标题：包含时间范围、分析主题（如《2025年Q3电商%s分析报告》）；
                2. 核心数据：提炼前3名的关键数据；
                3. 原因分析：简述领先原因；
                4. 业务建议：基于分析结果给出2-3条可落地的建议；
                5. 语言正式、专业，段落清晰（不要超过800字）。
                
                分析结论：%s
                用户原始需求：%s
                时间范围：%s
                """, state.getTargetIndicator(), analysisResult, userQuery, timeRange);

        String reportDraft = chatModel.call(prompt);
        System.out.println("报告草稿：" + reportDraft);

        return state.withReportDraft(reportDraft);
    }

    @Override
    public Map<String, Object> apply(AnalysisTaskState analysisTaskState) throws Exception {
        AnalysisTaskState result = this.generate(analysisTaskState);
        return result.toMap();
    }
}
