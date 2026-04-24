package com.zj.ai.langgraph4j.exception;

/**
 * 工作流执行异常
 * 当工作流执行失败时抛出
 *
 * @author zj
 * @date 2026/04/12
 */
public class WorkflowException extends RuntimeException {

    public WorkflowException(String message) {
        super(message);
    }

    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
