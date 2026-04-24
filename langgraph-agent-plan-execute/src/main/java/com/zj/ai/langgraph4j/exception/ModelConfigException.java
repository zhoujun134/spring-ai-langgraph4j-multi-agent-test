package com.zj.ai.langgraph4j.exception;

/**
 * 模型配置异常
 * 当模型配置不存在或无效时抛出
 *
 * @author zj
 * @date 2026/04/12
 */
public class ModelConfigException extends RuntimeException {

    public ModelConfigException(String message) {
        super(message);
    }

    public ModelConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
