package com.zj.ai.langgraph4j.config;

import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * LangGraph4j Studio 配置
 * 提供可视化工作流调试界面
 *
 * 访问地址: http://localhost:8081/?instance=default
 *
 * @author zj
 * @date 2026/04/19
 */
@Slf4j
@Configuration
public class StudioConfig extends LangGraphStudioConfig {

    private final StateGraph<PlanExecuteState> stateGraph;

    public StudioConfig(StateGraph<PlanExecuteState> stateGraph) {
        this.stateGraph = stateGraph;
    }

    @Override
    public Map<String, LangGraphStudioServer.Instance> instanceMap() {
        log.info("=== 初始化 LangGraph Studio 实例 ===");

        var instance = LangGraphStudioServer.Instance.builder()
                .title("Plan-Execute Agent Studio")
                .graph(stateGraph)
                .compileConfig(CompileConfig.builder()
                        .checkpointSaver(new MemorySaver())
                        .build())
                .addInputStringArg("input", true)
                .build();

        log.info("=== LangGraph Studio 实例初始化完成 ===");
        log.info("Studio 访问地址: http://localhost:8081/?instance=default");

        return Map.of("default", instance);
    }
}
