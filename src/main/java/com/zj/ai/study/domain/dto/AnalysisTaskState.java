package com.zj.ai.study.domain.dto;

import com.zj.ai.study.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.bsc.langgraph4j.state.AgentState;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局任务状态：所有智能体共享的数据载体
 */
@Getter
@Setter
public class AnalysisTaskState extends AgentState {
    // 基础信息
    private String userQuery;          // 用户原始需求
    // 任务拆解结果
    private String timeRange;          // 时间范围（如 2025年Q3）
    private String targetIndicator;    // 目标指标（如 销量、销售额）
    private String analysisDimension;  // 分析维度（如 品类、区域）
    // 中间结果
    private String rawData;            // 数据查询 Agent 输出的原始数据
    private String analysisResult;     // 数据分析 Agent 输出的分析结论
    private String reportDraft;        // 报告生成 Agent 输出的草稿
    // 最终结果
    private String finalReport;        // 校验通过的最终报告
    private boolean needRetry;        // 是否需要重试（结果校验失败时为 true）

    public AnalysisTaskState() {
        super(new HashMap<>());
    }

    public AnalysisTaskState(String userQuery, String timeRange, String targetIndicator,
            String analysisDimension, String rawData, String analysisResult, String reportDraft, String finalReport,
            boolean needRetry) {
        super(new HashMap<>());
        this.userQuery = userQuery;
        this.timeRange = timeRange;
        this.targetIndicator = targetIndicator;
        this.analysisDimension = analysisDimension;
        this.rawData = rawData;
        this.analysisResult = analysisResult;
        this.reportDraft = reportDraft;
        this.finalReport = finalReport;
        this.needRetry = needRetry;
    }

    public AnalysisTaskState(Map<String, Object> data) {
        super(data);
        AnalysisTaskState oldState =
                JSONUtils.parseObject(JSONUtils.toJSONString(data), AnalysisTaskState.class);
        if (oldState == null) {
            return;
        }
        this.userQuery = oldState.getUserQuery();
        this.timeRange = oldState.getTimeRange();
        this.targetIndicator = oldState.getTargetIndicator();
        this.analysisDimension = oldState.getAnalysisDimension();
        this.rawData = oldState.getRawData();
        this.analysisResult = oldState.getAnalysisResult();
        this.reportDraft = oldState.getReportDraft();
        this.finalReport = oldState.getFinalReport();
        this.needRetry = oldState.isNeedRetry();
    }

    public Map<String, Object> toMap() {
        return JSONUtils.convertToMap(this);
    }

    // 状态更新器（不可变对象，每次更新返回新实例）
    public AnalysisTaskState withTimeRange(String timeRange) {
       this.setTimeRange(timeRange);
       return this;
    }

    public AnalysisTaskState withTargetIndicator(String targetIndicator) {
        this.setTargetIndicator(targetIndicator);
        return this;
    }

    public AnalysisTaskState withAnalysisDimension(String analysisDimension) {
        this.setAnalysisDimension(analysisDimension);
        return this;
    }

    public AnalysisTaskState withRawData(String rawData) {
        this.setRawData(rawData);
        return this;
    }

    public AnalysisTaskState withAnalysisResult(String analysisResult) {
        this.setAnalysisResult(analysisResult);
        return this;
    }

    public AnalysisTaskState withReportDraft(String reportDraft) {
        this.setReportDraft(reportDraft);
        return this;
    }

    public AnalysisTaskState withFinalReport(String finalReport) {
        this.setFinalReport(finalReport);
        return this;
    }

    public AnalysisTaskState withNeedRetry(boolean needRetry) {
        this.setNeedRetry(needRetry);
        return this;
    }
}
