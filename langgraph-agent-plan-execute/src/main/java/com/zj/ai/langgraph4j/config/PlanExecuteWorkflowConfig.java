package com.zj.ai.langgraph4j.config;

import com.zj.ai.langgraph4j.agent.ExecuteAgent;
import com.zj.ai.langgraph4j.agent.PlanAgent;
import com.zj.ai.langgraph4j.agent.ReplanAgent;
import com.zj.ai.langgraph4j.agent.ValidateAgent;
import com.zj.ai.langgraph4j.agent.action.NodeActionEnum;
import com.zj.ai.langgraph4j.agent.edge.ExecutionEdge;
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
 * START → plan → validate → [条件边] → execute → [条件边] → END
 *                           ↓                ↓
 *                       replan ←────────────┘
 *                           ↓
 *                       validate (重新验证新计划)
 *
 * 条件边决策逻辑:
 * - PlanValidationEdge: 验证通过 → execute, 失败但可重规划 → replan, 其他 → END
 * - ExecutionEdge: 完成或部分成功 → END, 失败且可重规划 → replan, 其他 → END
 *
 * 防死循环机制:
 * - 最大重规划次数限制 (maxRePlanAttempts)
 * - 连续失败次数追踪 (consecutiveValidationFailures, consecutiveExecutionFailures)
 * - 重复计划检测 (lastPlanHash)
 * - 最大失败阈值检测 (hasExceededMaxFailures)
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
            ExecutionEdge executionEdge,
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

            // 4. 添加验证条件边
            // - 可行 → execute
            // - 不可行但可重规划 → replan
            // - 其他情况 → END
            graph.addConditionalEdges(
                    VALIDATE.getName(),
                    edge_async(validationEdge),
                    Map.of(
                            EXECUTE.getName(), EXECUTE.getName(),
                            RE_PLAN.getName(), RE_PLAN.getName(),
                            NodeActionEnum.END.getName(), END
                    )
            );

            // 5. 添加执行条件边
            // - 完成 → END
            // - 需要重规划 → replan
            graph.addConditionalEdges(
                    EXECUTE.getName(),
                    edge_async(executionEdge),
                    Map.of(
                            NodeActionEnum.END.getName(), END,
                            RE_PLAN.getName(), RE_PLAN.getName()
                    )
            );

            // 6. 重规划后重新验证
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
