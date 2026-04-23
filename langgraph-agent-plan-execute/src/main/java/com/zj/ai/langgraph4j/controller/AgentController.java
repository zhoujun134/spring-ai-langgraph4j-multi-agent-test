package com.zj.ai.langgraph4j.controller;

import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
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
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody QueryRequest request) {
        log.info("收到查询请求: {}", request.getQuery());

        try {
            // 创建初始状态
            PlanExecuteState state = new PlanExecuteState();
            state.setUserQuery(request.getQuery());
            state.setMaxRePlanAttempts(request.getMaxReplanAttempts() != null ? request.getMaxReplanAttempts() : 3);

            // 执行工作流
            PlanExecuteState result = planExecuteWorkflow.invoke(state.toMap())
                    .orElseThrow(() -> new RuntimeException("工作流执行返回空结果"));

            // 构建响应
            return ResponseEntity.ok(Map.of(
                    "success", result.isCompleted(),
                    "query", request.getQuery(),
                    "plan", result.getPlanSteps(),
                    "executionResults", result.getExecutionResults(),
                    "finalAnswer", result.getFinalAnswer(),
                    "errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : ""
            ));

        } catch (Exception e) {
            log.error("查询执行失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 查询请求
     */
    public static class QueryRequest {
        private String query;
        private Integer maxReplanAttempts;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Integer getMaxReplanAttempts() {
            return maxReplanAttempts;
        }

        public void setMaxReplanAttempts(Integer maxReplanAttempts) {
            this.maxReplanAttempts = maxReplanAttempts;
        }
    }
}
