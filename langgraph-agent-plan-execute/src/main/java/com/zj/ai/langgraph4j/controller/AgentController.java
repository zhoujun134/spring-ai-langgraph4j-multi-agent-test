package com.zj.ai.langgraph4j.controller;

import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import com.zj.ai.langgraph4j.exception.WorkflowException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Agent API 控制器
 * 提供 REST API 接口调用 Plan-Execute 工作流
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final CompiledGraph<PlanExecuteState> planExecuteWorkflow;

    /**
     * 执行查询
     *
     * @param request 查询请求
     * @return 执行结果
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> executeQuery(@Valid @RequestBody QueryRequest request) {
        log.info("收到查询请求: {}", request.getQuery());

        try {
            // 创建初始状态
            PlanExecuteState state = new PlanExecuteState();
            state.setUserQuery(request.getQuery());
            state.setMaxRePlanAttempts(request.getMaxReplanAttempts() != null ? request.getMaxReplanAttempts() : 3);

            // 执行工作流
            PlanExecuteState result = planExecuteWorkflow.invoke(state.toMap())
                    .orElseThrow(() -> new WorkflowException("工作流执行返回空结果"));

            // 构建响应
            return ResponseEntity.ok(Map.of(
                    "success", result.isCompleted(),
                    "query", request.getQuery(),
                    "plan", result.getPlanSteps(),
                    "executionResults", result.getExecutionResults(),
                    "finalAnswer", result.getFinalAnswer(),
                    "errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : ""
            ));

        } catch (WorkflowException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询执行失败", e);
            throw new WorkflowException("查询执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "plan-execute-agent"
        ));
    }

    /**
     * 查询请求
     */
    @Data
    public static class QueryRequest {
        @NotBlank(message = "查询内容不能为空")
        @Size(max = 2000, message = "查询内容不能超过2000字符")
        private String query;

        @Min(value = 1, message = "最大重规划次数不能小于1")
        @Max(value = 10, message = "最大重规划次数不能超过10")
        private Integer maxReplanAttempts;
    }
}
