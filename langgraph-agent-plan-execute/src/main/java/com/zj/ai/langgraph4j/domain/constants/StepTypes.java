package com.zj.ai.langgraph4j.domain.constants;

/**
 * @ClassName StepTypes
 * @Author zj
 * @Description 步骤类型枚举
 * @Date 2026/4/14 22:38
 * @Version v1.0
 **/
public enum StepTypes {
    /**
     * 工具执行步骤
     */
    TOOL_EXECUTION,
    /**
     * 验证步骤
     */
    VALIDATION,
    /**
     * SQL 查询步骤
     */
    SQL_QUERY,
    /**
     * 结果分析步骤
     */
    RESULT_ANALYSIS,
}
