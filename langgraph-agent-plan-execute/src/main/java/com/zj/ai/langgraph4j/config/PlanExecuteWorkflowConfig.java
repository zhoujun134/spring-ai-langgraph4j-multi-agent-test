package com.zj.ai.langgraph4j.config;

import com.zj.ai.langgraph4j.agent.*;
import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import com.zj.ai.langgraph4j.edge.PlanValidationEdge;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Plan-Execute 工作流配置
 * 定义完整的状态图工作流
 *
 * 工作流程:
 * START → plan → validate → [条件边] → execute → END
 *                           ↓
 *                       replan → validate (重新验证新计划)
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Configuration
public class PlanExecuteWorkflowConfig {

    /**
     * 创建 Plan-Execute 工作流
     */
    @Bean
    public CompiledGraph<PlanExecuteState> planExecuteWorkflow(
            PlanAgent planAgent,
            ValidateAgent validateAgent,
            ExecuteAgent executeAgent,
            ReplanAgent replanAgent,
            PlanValidationEdge validationEdge,
            PlanExecuteStateFactory stateFactory) {

        log.info("=== 初始化 Plan-Execute 工作流 ===");

        try {
            // 1. 创建 StateGraph
            StateGraph<PlanExecuteState> graph = new StateGraph<>(stateFactory);

            // 2. 添加节点
            graph.addNode("plan", node_async(planAgent));
            graph.addNode("validate", node_async(validateAgent));
            graph.addNode("execute", node_async(executeAgent));
            graph.addNode("replan", node_async(replanAgent));

            // 3. 添加边
            // START → plan
            graph.addEdge(START, "plan");
            // plan → validate
            graph.addEdge("plan", "validate");

            // 4. 添加条件边 (validate 的分支)
            // - 可行 → execute
            // - 不可行但可重规划 → replan
            // - 不可行且不可重规划 → END
            graph.addConditionalEdges(
                    "validate",
                    edge_async(validationEdge),
                    Map.of(
                            "execute", "execute",
                            "replan", "replan",
                            "end", END
                    )
            );

            // 5. 完成边
            // execute → END
            graph.addEdge("execute", END);
            // replan → validate (重新规划后直接验证，不需要重新制定计划)
            graph.addEdge("replan", "validate");

            // 6. 编译工作流
            CompiledGraph<PlanExecuteState> compiled = graph.compile();

            log.info("=== Plan-Execute 工作流初始化完成 ===");

            return compiled;

        } catch (Exception e) {
            log.error("工作流初始化失败", e);
            throw new RuntimeException("Failed to create plan-execute workflow", e);
        }
    }
}
