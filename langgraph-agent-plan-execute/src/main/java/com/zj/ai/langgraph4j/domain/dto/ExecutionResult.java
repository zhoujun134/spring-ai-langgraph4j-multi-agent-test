package com.zj.ai.langgraph4j.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 执行结果
 * 表示工具执行后的结果
 *
 * @author zj
 * @date 2026/04/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对应的计划步骤索引
     */
    private int stepIndex;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 执行结果
     */
    private Object result;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建成功结果
     */
    public static ExecutionResult success(int stepIndex, String toolName, Object result) {
        return new ExecutionResult(stepIndex, toolName, result, true, null);
    }

    /**
     * 创建失败结果
     */
    public static ExecutionResult failure(int stepIndex, String toolName, String errorMessage) {
        return new ExecutionResult(stepIndex, toolName, null, false, errorMessage);
    }
}
