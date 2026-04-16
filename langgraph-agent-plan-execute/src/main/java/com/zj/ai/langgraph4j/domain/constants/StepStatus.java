package com.zj.ai.langgraph4j.domain.constants;

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
