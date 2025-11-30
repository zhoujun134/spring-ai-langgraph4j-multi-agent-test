package com.zj.ai.study.service.config;

import com.zj.ai.study.domain.dto.AnalysisTaskState;
import com.zj.ai.study.service.agent.*;
import com.zj.ai.study.service.edge.RetryEdgeAction;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 多智能体流程配置：用 LangGraph4j 定义节点流转规则
 */
@Configuration
public class AgentWorkflowConfig {
    @Bean
    public CompiledGraph<AnalysisTaskState> analysisWorkflow(
            TaskDecompositionAgent decompositionAgent,
            DataQueryAgent dataQueryAgent,
            DataAnalysisAgent dataAnalysisAgent,
            ReportGenerationAgent reportGenerationAgent,
            ResultValidationAgent validationAgent,
            RetryEdgeAction retryEdgeAction,
            AnalysisTaskStateFactory stateFactory
    ) throws GraphStateException {

        // 1. 创建状态图构建器（指定状态类型）
        StateGraph<AnalysisTaskState> graphBuilder = new StateGraph<>(stateFactory);

        // 2. 添加节点（每个智能体对应一个节点）
        graphBuilder.addNode("decompose", node_async(decompositionAgent)); // 任务拆解
        graphBuilder.addNode("query", node_async(dataQueryAgent)); // 数据查询
        graphBuilder.addNode("analyze", node_async(dataAnalysisAgent)); // 数据分析
        graphBuilder.addNode("generateReport",node_async(reportGenerationAgent)); // 报告生成
        graphBuilder.addNode("validate", node_async(validationAgent)); // 结果校验

        // 3. 定义流程流转规则（边）
        // 起始节点 → 任务拆解节点
        graphBuilder.addEdge(START, "decompose");

        // 任务拆解 → 数据查询 → 数据分析 → 报告生成 → 结果校验
        graphBuilder.addEdge("decompose", "query");
        graphBuilder.addEdge("query", "analyze");
        graphBuilder.addEdge("analyze", "generateReport");
        graphBuilder.addEdge("generateReport", "validate");

        // 结果校验 → 分支判断（符合则结束，不符合则重试任务拆解）
        graphBuilder.addConditionalEdges(
                "validate",
                // 分支判断逻辑：根据 needRetry 决定下一个节点
                AsyncEdgeAction.edge_async(retryEdgeAction),
                // 映射：判断结果 → 目标节点（重试→任务拆解，结束→终止流程）
                Map.of(
                        "retry", "decompose",
                        "end", END
                )
        );

        // 4. 构建并返回状态图
        return graphBuilder.compile();
    }
}
