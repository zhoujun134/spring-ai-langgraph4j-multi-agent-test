package com.zj.ai.langgraph4j.domain.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 提供商枚举
 * 支持的 LLM 提供商类型
 *
 * @author zj
 * @date 2026/04/12
 */
@AllArgsConstructor
@Getter
public enum AiProviderEnum {

    OLLAMA("ollama"),

    OPENAI("openai"),

    DEEPSEEK("deepseek"),

    ;

    private final String provider;
}
