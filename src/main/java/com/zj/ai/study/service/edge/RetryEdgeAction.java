package com.zj.ai.study.service.edge;

import com.zj.ai.study.domain.dto.AnalysisTaskState;
import org.bsc.langgraph4j.action.EdgeAction;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhoujun09@kuaishou.com
 * Created on 2025-11-27
 */
@Slf4j
@Component
public class RetryEdgeAction implements EdgeAction<AnalysisTaskState> {

    @Override
    public String apply(AnalysisTaskState analysisTaskState) throws Exception {
        return analysisTaskState.isNeedRetry() ? "retry" : "end";
    }
}
