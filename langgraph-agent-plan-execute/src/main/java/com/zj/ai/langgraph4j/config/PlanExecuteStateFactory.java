package com.zj.ai.langgraph4j.config;

import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import org.bsc.langgraph4j.state.AgentStateFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * PlanExecuteState 工厂
 * 用于从 Map 创建状态实例
 *
 * @author zj
 * @date 2026/04/12
 */
@Component
public class PlanExecuteStateFactory implements AgentStateFactory<PlanExecuteState> {

    @Override
    public PlanExecuteState apply(Map<String, Object> data) {
        return new PlanExecuteState(data);
    }
}
