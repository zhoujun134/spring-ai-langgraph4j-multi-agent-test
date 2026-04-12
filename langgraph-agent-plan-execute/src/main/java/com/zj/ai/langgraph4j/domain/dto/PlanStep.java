package com.zj.ai.langgraph4j.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 计划步骤
 * 表示计划中的一个执行步骤
 *
 * @author zj
 * @date 2026/04/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanStep implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 步骤索引
     */
    private int stepIndex;

    /**
     * 步骤描述
     */
    private String description;

    /**
     * 使用的工具名称
     */
    private String toolName;

    /**
     * 工具输入参数
     */
    private String toolInput;

    /**
     * 预期输出
     */
    private String expectedOutput;

    /**
     * 步骤状态
     */
    private StepStatus status = StepStatus.PENDING;

    /**
     * 步骤状态枚举
     */
    public enum StepStatus {
        /**
         * 待执行
         */
        PENDING,
        /**
         * 执行中
         */
        EXECUTING,
        /**
         * 已完成
         */
        COMPLETED,
        /**
         * 失败
         */
        FAILED
    }

    /**
     * 创建待执行步骤
     */
    public static PlanStep pending(int index, String description, String toolName, String toolInput) {
        return new PlanStep(index, description, toolName, toolInput, null, StepStatus.PENDING);
    }

    /**
     * 标记为执行中
     */
    public void markExecuting() {
        this.status = StepStatus.EXECUTING;
    }

    /**
     * 标记为完成
     */
    public void markCompleted() {
        this.status = StepStatus.COMPLETED;
    }

    /**
     * 标记为失败
     */
    public void markFailed() {
        this.status = StepStatus.FAILED;
    }
}
