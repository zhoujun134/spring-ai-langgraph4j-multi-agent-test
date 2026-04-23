package com.zj.ai.langgraph4j.config;

import com.zj.ai.langgraph4j.agent.ExecuteAgent;
import com.zj.ai.langgraph4j.agent.PlanAgent;
import com.zj.ai.langgraph4j.agent.ReplanAgent;
import com.zj.ai.langgraph4j.agent.ValidateAgent;
import com.zj.ai.langgraph4j.agent.action.NodeActionEnum;
import com.zj.ai.langgraph4j.agent.edge.PlanValidationEdge;
import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.zj.ai.langgraph4j.agent.action.NodeActionEnum.*;
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
     * 创建 StateGraph（供 Studio 使用）
     */
    @Bean
    public StateGraph<PlanExecuteState> planExecuteStateGraph(
            PlanAgent planAgent,
            ValidateAgent validateAgent,
            ExecuteAgent executeAgent,
            ReplanAgent replanAgent,
            PlanValidationEdge validationEdge,
            PlanExecuteStateFactory stateFactory) {

        log.info("=== 创建 Plan-Execute StateGraph ===");

        try {
            // 1. 创建 StateGraph
            StateGraph<PlanExecuteState> graph = new StateGraph<>(stateFactory);

            // 2. 添加节点
            graph.addNode(PLAN.getName(), node_async(planAgent));
            graph.addNode(VALIDATE.getName(), node_async(validateAgent));
            graph.addNode(EXECUTE.getName(), node_async(executeAgent));
            graph.addNode(RE_PLAN.getName(), node_async(replanAgent));

            // 3. 添加边
            // START → plan
            graph.addEdge(START, PLAN.getName());
            // plan → validate
            graph.addEdge(PLAN.getName(), VALIDATE.getName());

            // 4. 添加条件边 (validate 的分支)
            // - 可行 → execute
            // - 不可行但可重规划 → replan
            // - 不可行且不可重规划 → END
            graph.addConditionalEdges(
                    VALIDATE.getName(),
                    edge_async(validationEdge),
                    Map.of(
                            EXECUTE.getName(), EXECUTE.getName(),
                            RE_PLAN.getName(), RE_PLAN.getName(),
                            NodeActionEnum.END.getName(), END
                    )
            );

            // 5. 完成边
            // execute → END
            graph.addEdge(EXECUTE.getName(), END);
            // replan → validate (重新规划后直接验证，不需要重新制定计划)
            graph.addEdge(RE_PLAN.getName(), VALIDATE.getName());

            log.info("=== Plan-Execute StateGraph 创建完成 ===");

            return graph;

        } catch (Exception e) {
            log.error("StateGraph 创建失败", e);
            throw new RuntimeException("Failed to create plan-execute state graph", e);
        }
    }

    /**
     * 创建 Plan-Execute 工作流
     */
    @Bean
    public CompiledGraph<PlanExecuteState> planExecuteWorkflow(
            StateGraph<PlanExecuteState> graph) {

        log.info("=== 编译 Plan-Execute 工作流 ===");

        try {
            // 编译工作流，添加 MemorySaver 用于 studio 持久化
            CompileConfig compileConfig = CompileConfig.builder()
                    .checkpointSaver(new MemorySaver())
                    .build();

            CompiledGraph<PlanExecuteState> compiled = graph.compile(compileConfig);

            log.info("=== Plan-Execute 工作流初始化完成 ===");

            return compiled;

        } catch (Exception e) {
            log.error("工作流初始化失败", e);
            throw new RuntimeException("Failed to create plan-execute workflow", e);
        }
    }
}
