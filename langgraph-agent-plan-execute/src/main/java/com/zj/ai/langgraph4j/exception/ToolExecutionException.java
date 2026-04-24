package com.zj.ai.langgraph4j.exception;

/**
 * 工具执行异常
 * 当工具不存在或执行失败时抛出
 *
 * @author zj
 * @date 2026/04/12
 */
public class ToolExecutionException extends RuntimeException {

    public ToolExecutionException(String message) {
        super(message);
    }

    public ToolExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
