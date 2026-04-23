package com.zj.ai.langgraph4j.domain.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName AiProviderEnmu
 * @Author zhoujun134
 * @Description
 * @Date 2026/4/23 22:04
 * @Version v1.0
 **/
@AllArgsConstructor
@Getter
public enum AiProviderEnum {

    OLLAMA("ollama"),

    OPENAI("openai"),

    ;
    private final String provider;
}
