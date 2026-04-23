package com.zj.ai.langgraph4j.agent.action;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName NodeActionEnum
 * @Author zhoujun134
 * @Description
 * @Date 2026/4/23 22:39
 * @Version v1.0
 **/
@AllArgsConstructor
@Getter
public enum NodeActionEnum {

    PLAN("plan"),
    VALIDATE("validate"),
    EXECUTE("execute"),
    RE_PLAN("replan"),
    END("end"),
    ;

    private final String name;
}
