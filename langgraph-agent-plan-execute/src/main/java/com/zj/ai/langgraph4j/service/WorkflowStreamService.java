package com.zj.ai.langgraph4j.service;

import com.zj.ai.langgraph4j.domain.dto.ExecutionResult;
import com.zj.ai.langgraph4j.domain.dto.PlanStep;
import com.zj.ai.langgraph4j.domain.dto.WorkflowEvent;
import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import com.zj.ai.langgraph4j.exception.WorkflowException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.zj.ai.langgraph4j.agent.action.NodeActionEnum.*;

/**
 * 工作流流式执行服务
 * 通过 SSE 推送工作流执行进度
 *
 * @author zj
 * @date 2026/04/24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowStreamService {

    private final CompiledGraph<PlanExecuteState> planExecuteWorkflow;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 流式执行工作流
     * 使用 stream() 方法实时输出每个节点的内容
     *
     * @param query     用户查询
     * @param maxReplan 最大重规划次数
     * @return SSE Emitter
     */
    public SseEmitter executeStream(String query, Integer maxReplan) {
        SseEmitter emitter = new SseEmitter(300000L);
        long startTime = System.currentTimeMillis();

        executor.execute(() -> {
            try {
                PlanExecuteState state = new PlanExecuteState();
                state.setUserQuery(query);
                state.setMaxRePlanAttempts(maxReplan != null ? maxReplan : 3);

                emitEvent(emitter, WorkflowEvent.of(
                        WorkflowEvent.EventType.WORKFLOW_STARTED,
                        "workflow",
                        "开始执行工作流"
                ));

                // 使用 stream() 方法实时获取每个节点的输出
                AsyncGenerator<NodeOutput<PlanExecuteState>> generator = planExecuteWorkflow.stream(state.toMap());

                PlanExecuteState finalState = null;
                for (NodeOutput<PlanExecuteState> output : generator) {
                    if (output == null) continue;

                    String nodeName = output.node();
                    PlanExecuteState currentState = output.state();

                    log.debug("节点执行完成: {}", nodeName);

                    // 根据节点类型发送不同的事件
                    if (nodeName != null && !nodeName.equals("__START__") && !nodeName.equals("__END__")) {
                        emitNodeEvent(emitter, nodeName, currentState);
                    }

                    finalState = currentState;
                }

                if (finalState == null) {
                    throw new WorkflowException("工作流执行返回空结果");
                }

                // 发送最终完成事件
                Map<String, Object> finalData = new HashMap<>();
                finalData.put("success", finalState.isCompleted());
                finalData.put("finalAnswer", finalState.getFinalAnswer());
                finalData.put("errorMessage", finalState.getErrorMessage());
                finalData.put("duration", System.currentTimeMillis() - startTime);

                emitEvent(emitter, WorkflowEvent.of(
                        WorkflowEvent.EventType.WORKFLOW_COMPLETE,
                        "workflow",
                        finalState.isCompleted() ? "工作流执行完成" : "工作流执行失败",
                        finalData
                ));

                emitter.complete();

            } catch (Exception e) {
                log.error("工作流执行失败", e);
                try {
                    emitEvent(emitter, WorkflowEvent.of(
                            WorkflowEvent.EventType.ERROR,
                            "workflow",
                            "执行失败: " + e.getMessage()
                    ));
                    emitter.complete();
                } catch (Exception ignored) {
                }
            }
        });

        emitter.onCompletion(() -> log.info("SSE 连接关闭"));
        emitter.onTimeout(() -> log.warn("SSE 连接超时"));
        emitter.onError(e -> log.error("SSE 连接错误: {}", e.getMessage()));

        return emitter;
    }

    /**
     * 流式执行工作流（带实时步骤回调）
     * 使用 stream() 方法实时输出每个节点的内容
     */
    public SseEmitter executeStreamWithSteps(String query, Integer maxReplan) {
        SseEmitter emitter = new SseEmitter(300000L);
        long startTime = System.currentTimeMillis();

        executor.execute(() -> {
            try {
                PlanExecuteState state = new PlanExecuteState();
                state.setUserQuery(query);
                state.setMaxRePlanAttempts(maxReplan != null ? maxReplan : 3);

                emitEvent(emitter, WorkflowEvent.of(
                        WorkflowEvent.EventType.WORKFLOW_STARTED,
                        "workflow",
                        "开始执行工作流",
                        Map.of("query", query, "startTime", startTime)
                ));

                // 使用 stream() 方法实时获取每个节点的输出
                AsyncGenerator<NodeOutput<PlanExecuteState>> generator = planExecuteWorkflow.stream(state.toMap());

                PlanExecuteState finalState = null;
                for (NodeOutput<PlanExecuteState> output : generator) {
                    if (output == null) continue;

                    String nodeName = output.node();
                    PlanExecuteState currentState = output.state();

                    log.info("节点执行完成: {}", nodeName);

                    // 根据节点类型发送不同的事件
                    if (nodeName != null && !nodeName.equals("__START__") && !nodeName.equals("__END__")) {
                        emitNodeEvent(emitter, nodeName, currentState);
                    }

                    finalState = currentState;
                }

                if (finalState == null) {
                    throw new WorkflowException("工作流执行返回空结果");
                }

                // 发送最终完成事件
                long duration = System.currentTimeMillis() - startTime;
                Map<String, Object> finalData = new HashMap<>();
                finalData.put("success", finalState.isCompleted());
                finalData.put("finalAnswer", finalState.getFinalAnswer());
                finalData.put("duration", duration);
                finalData.put("replanCount", finalState.getRePlanCount());

                emitEvent(emitter, WorkflowEvent.of(
                        WorkflowEvent.EventType.WORKFLOW_COMPLETE,
                        "workflow",
                        finalState.isCompleted() ? "工作流执行完成" : "工作流执行结束",
                        finalData
                ));

                emitter.complete();

            } catch (Exception e) {
                log.error("工作流执行失败", e);
                try {
                    emitEvent(emitter, WorkflowEvent.of(
                            WorkflowEvent.EventType.ERROR,
                            "workflow",
                            "执行失败: " + e.getMessage()
                    ));
                    emitter.complete();
                } catch (Exception ignored) {
                }
            }
        });

        emitter.onCompletion(() -> log.info("SSE 连接关闭"));
        emitter.onTimeout(() -> log.warn("SSE 连接超时"));
        emitter.onError(e -> log.error("SSE 连接错误: {}", e.getMessage()));

        return emitter;
    }

    /**
     * 根据节点类型发送相应的事件
     */
    private void emitNodeEvent(SseEmitter emitter, String nodeName, PlanExecuteState state) throws IOException {
        if (PLAN.getName().equals(nodeName)) {
            // 计划生成完成
            List<PlanStep> steps = state.getPlanSteps();
            if (steps != null && !steps.isEmpty()) {
                emitEvent(emitter, WorkflowEvent.of(
                        WorkflowEvent.EventType.PLAN_CREATED,
                        "plan",
                        "计划生成完成，共 " + steps.size() + " 个步骤",
                        Map.of("steps", steps, "totalSteps", steps.size())
                ));
            }
        } else if (VALIDATE.getName().equals(nodeName)) {
            // 验证完成
            emitEvent(emitter, WorkflowEvent.of(
                    WorkflowEvent.EventType.VALIDATION_COMPLETE,
                    "validate",
                    state.getValidationResult(),
                    Map.of("feasible", state.isPlanFeasible())
            ));
        } else if (EXECUTE.getName().equals(nodeName)) {
            // 执行完成 - 发送每个步骤的执行结果
            List<ExecutionResult> results = state.getExecutionResults();
            if (results != null && !results.isEmpty()) {
                // 只发送最新完成的步骤
                ExecutionResult lastResult = results.getLast();
                List<PlanStep> steps = state.getPlanSteps();
                int stepIndex = results.size();
                String stepDesc = steps != null && stepIndex <= steps.size()
                        ? steps.get(stepIndex - 1).getDescription()
                        : "步骤 " + stepIndex;

                if (lastResult.isSuccess()) {
                    emitEvent(emitter, WorkflowEvent.of(
                            WorkflowEvent.EventType.STEP_COMPLETED,
                            "execute",
                            stepDesc + " 执行成功",
                            Map.of(
                                    "stepIndex", stepIndex,
                                    "toolName", lastResult.getToolName(),
                                    "result", lastResult.getResult()
                            )
                    ));
                } else {
                    emitEvent(emitter, WorkflowEvent.of(
                            WorkflowEvent.EventType.STEP_FAILED,
                            "execute",
                            stepDesc + " 执行失败",
                            Map.of(
                                    "stepIndex", stepIndex,
                                    "toolName", lastResult.getToolName(),
                                    "error", lastResult.getErrorMessage()
                            )
                    ));
                }
            }
        } else if (RE_PLAN.getName().equals(nodeName)) {
            // 重规划触发
            emitEvent(emitter, WorkflowEvent.of(
                    WorkflowEvent.EventType.REPLAN_TRIGGERED,
                    "replan",
                    "触发重新规划，第 " + (state.getRePlanCount() + 1) + " 次重规划",
                    Map.of("replanCount", state.getRePlanCount() + 1)
            ));
        }
    }

    private void emitEvent(SseEmitter emitter, WorkflowEvent event) throws IOException {
        emitter.send(SseEmitter.event()
                .name(event.getEventType().name())
                .data(event));
    }
}
