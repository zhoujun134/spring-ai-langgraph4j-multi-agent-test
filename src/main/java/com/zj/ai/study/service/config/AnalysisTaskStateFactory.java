package com.zj.ai.study.service.config;

import com.zj.ai.study.domain.dto.AnalysisTaskState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.state.AgentStateFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author zhoujun09@kuaishou.com
 * Created on 2025-11-27
 */
@Slf4j
@Component
public class AnalysisTaskStateFactory implements AgentStateFactory<AnalysisTaskState> {
    @Override
    public AnalysisTaskState apply(Map<String, Object> stringObjectMap) {
        return new AnalysisTaskState(stringObjectMap);
    }
}
