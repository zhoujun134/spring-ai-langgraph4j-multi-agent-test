package com.zj.ai.study.service.agent;

import com.zj.ai.study.domain.dto.AnalysisTaskState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 结果校验 Agent：检查报告是否满足用户需求，不满足则标记重试
 */
@Component
public class ResultValidationAgent implements NodeAction<AnalysisTaskState> {

    private final ChatModel chatModel;

    public ResultValidationAgent(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public AnalysisTaskState validate(AnalysisTaskState state) {
        String userQuery = state.getUserQuery();
        String reportDraft = state.getReportDraft();

        // 构建提示词：校验报告是否符合需求
        String prompt = String.format("""
                请判断以下报告是否完全满足用户需求，仅返回结论（二选一）：
                1. 符合：报告包含用户需求的所有关键点，数据准确、结构完整；
                2. 不符合：报告缺少关键信息、数据错误或未回应核心需求。
                
                用户需求：%s
                报告内容：%s
                
                仅返回「符合」或「不符合」，不要额外内容！
                """, userQuery, reportDraft);

        String validationResult = chatModel.call(prompt);
        System.out.println("报告校验结果：" + validationResult);

        // 标记是否需要重试
        boolean needRetry = "不符合".equals(validationResult.trim());
        if (needRetry) {
            return state.withNeedRetry(true);
        } else {
            return state.withFinalReport(reportDraft).withNeedRetry(false);
        }
    }
    @Override
    public Map<String, Object> apply(AnalysisTaskState analysisTaskState) throws Exception {
        AnalysisTaskState result = this.validate(analysisTaskState);
        return result.toMap();
    }
}
