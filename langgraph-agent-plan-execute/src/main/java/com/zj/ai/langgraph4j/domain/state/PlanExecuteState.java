package com.zj.ai.langgraph4j.domain.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zj.ai.langgraph4j.domain.dto.ExecutionResult;
import com.zj.ai.langgraph4j.domain.dto.PlanStep;
import lombok.Getter;
import lombok.Setter;
import org.bsc.langgraph4j.state.AgentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plan-Execute 工作流状态
 * 在整个工作流中传递和更新的状态对象
 *
 * @author zj
 * @date 2026/04/12
 */
@Getter
@Setter
public class PlanExecuteState extends AgentState {

    // ==================== 输入 ====================

    /**
     * 用户原始问题
     */
    private String userQuery;

    // ==================== 计划相关 ====================

    /**
     * 生成的计划步骤
     */
    private List<PlanStep> plan = new ArrayList<>();

    /**
     * 当前执行步骤索引
     */
    private int currentStepIndex = 0;

    /**
     * 验证结果
     */
    private String validationResult;

    /**
     * 计划是否可行
     */
    private boolean planFeasible = false;

    /**
     * 重新规划次数
     */
    private int replanCount = 0;

    /**
     * 最大重新规划次数
     */
    private int maxReplanAttempts = 3;

    // ==================== 执行相关 ====================

    /**
     * 执行结果列表
     */
    private List<ExecutionResult> executionResults = new ArrayList<>();

    /**
     * 当前使用的工具
     */
    private String currentTool;

    /**
     * 当前工具输入
     */
    private String currentToolInput;

    // ==================== 输出 ====================

    /**
     * 最终答案
     */
    private String finalAnswer;

    /**
     * 是否完成
     */
    private boolean completed = false;

    // ==================== 错误处理 ====================

    /**
     * 错误信息
     */
    private String errorMessage;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public PlanExecuteState() {
        super(new HashMap<>());
    }

    /**
     * 从 Map 构造（用于状态反序列化）
     */
    public PlanExecuteState(Map<String, Object> data) {
        super(data);
        parseFromMap(data);
    }

    /**
     * 更新内部 data Map（确保与字段同步）
     */
    private void updateInternalData() {
        Map<String, Object> data = this.data();
        if (data != null) {
            data.put("replanCount", replanCount);
            data.put("planFeasible", planFeasible);
            data.put("validationResult", validationResult);
            data.put("plan", plan);
            data.put("completed", completed);
            data.put("finalAnswer", finalAnswer);
            data.put("errorMessage", errorMessage);
            data.put("executionResults", executionResults);
        }
    }

    // ==================== 解析方法 ====================

    /**
     * 从 Map 解析字段
     */
    @SuppressWarnings("unchecked")
    private void parseFromMap(Map<String, Object> data) {
        this.userQuery = (String) data.get("userQuery");
        this.validationResult = (String) data.get("validationResult");
        this.planFeasible = Boolean.TRUE.equals(data.get("planFeasible"));
        this.replanCount = data.get("replanCount") != null ? (Integer) data.get("replanCount") : 0;
        this.maxReplanAttempts = data.get("maxReplanAttempts") != null ? (Integer) data.get("maxReplanAttempts") : 3;
        this.currentStepIndex = data.get("currentStepIndex") != null ? (Integer) data.get("currentStepIndex") : 0;
        this.finalAnswer = (String) data.get("finalAnswer");
        this.completed = Boolean.TRUE.equals(data.get("completed"));
        this.errorMessage = (String) data.get("errorMessage");
        this.currentTool = (String) data.get("currentTool");
        this.currentToolInput = (String) data.get("currentToolInput");

        // 解析计划步骤列表
        Object planObj = data.get("plan");
        if (planObj instanceof List) {
            this.plan = new ArrayList<>();
            for (Object item : (List<?>) planObj) {
                if (item instanceof PlanStep) {
                    this.plan.add((PlanStep) item);
                }
            }
        }

        // 解析执行结果列表
        Object resultsObj = data.get("executionResults");
        if (resultsObj instanceof List) {
            this.executionResults = new ArrayList<>();
            for (Object item : (List<?>) resultsObj) {
                if (item instanceof ExecutionResult) {
                    this.executionResults.add((ExecutionResult) item);
                }
            }
        }
    }

    // ==================== 转换方法 ====================

    /**
     * 转换为 Map（用于状态持久化）
     * 同时更新内部 data Map 以保持同步
     */
    public Map<String, Object> toMap() {
        // 先更新内部数据
        updateInternalData();

        // 返回新的 Map
        Map<String, Object> map = new HashMap<>();
        map.put("userQuery", userQuery);
        map.put("plan", plan);
        map.put("currentStepIndex", currentStepIndex);
        map.put("validationResult", validationResult);
        map.put("planFeasible", planFeasible);
        map.put("replanCount", replanCount);
        map.put("maxReplanAttempts", maxReplanAttempts);
        map.put("executionResults", executionResults);
        map.put("currentTool", currentTool);
        map.put("currentToolInput", currentToolInput);
        map.put("finalAnswer", finalAnswer);
        map.put("completed", completed);
        map.put("errorMessage", errorMessage);
        return map;
    }

    // ==================== 流式更新方法 ====================

    /**
     * 设置计划
     */
    public PlanExecuteState withPlan(List<PlanStep> plan) {
        this.plan = plan != null ? plan : new ArrayList<>();
        return this;
    }

    /**
     * 设置验证结果
     */
    public PlanExecuteState withValidationResult(String result, boolean feasible) {
        this.validationResult = result;
        this.planFeasible = feasible;
        return this;
    }

    /**
     * 增加重规划计数
     */
    public PlanExecuteState incrementReplanCount() {
        this.replanCount++;
        return this;
    }

    /**
     * 添加执行结果
     */
    public PlanExecuteState addExecutionResult(ExecutionResult result) {
        if (this.executionResults == null) {
            this.executionResults = new ArrayList<>();
        }
        this.executionResults.add(result);
        return this;
    }

    /**
     * 设置最终答案并标记完成
     */
    public PlanExecuteState withFinalAnswer(String answer) {
        this.finalAnswer = answer;
        this.completed = true;
        return this;
    }

    /**
     * 设置错误信息
     */
    public PlanExecuteState withError(String error) {
        this.errorMessage = error;
        return this;
    }

    // ==================== 辅助方法 ====================

    /**
     * 是否可以重新规划
     */
    @JsonIgnore
    public boolean canReplan() {
        return replanCount < maxReplanAttempts;
    }

    /**
     * 获取当前步骤
     */
    @JsonIgnore
    public PlanStep getCurrentStep() {
        if (plan != null && currentStepIndex < plan.size()) {
            return plan.get(currentStepIndex);
        }
        return null;
    }

    /**
     * 是否有计划
     */
    @JsonIgnore
    public boolean hasPlan() {
        return plan != null && !plan.isEmpty();
    }
}
