package com.zj.ai.langgraph4j.domain.state.step;

import com.zj.ai.langgraph4j.domain.constants.ToolTypeEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @ClassName ToolExecutionDesc
 * @Author zj
 * @Description
 * @Date 2026/4/14 22:42
 * @Version v1.0
 **/
@Data
public class ToolInvokeDesc implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 工具 id
     */
    private String toolId;
    /**
     * 工具名称
     */
    private String toolName;
    /**
     * 工具描述
     */
    private String toolDescription;
    /**
     * 工具类型
     */
    private ToolTypeEnum toolType;
    /**
     * 工具输入参数
     */
    private String toolInput;

    /**
     * 预期输出
     */
    private String expectedOutput;
}
