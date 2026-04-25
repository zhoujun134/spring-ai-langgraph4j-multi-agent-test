package com.zj.ai.langgraph4j.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 工作流事件
 * 用于 SSE 流式传输工作流执行进度
 *
 * @author zj
 * @date 2026/04/24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * 当前节点名称
     */
    private String node;

    /**
     * 事件消息
     */
    private String message;

    /**
     * 事件数据
     */
    private Object data;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 事件类型枚举
     */
    public enum EventType {
        WORKFLOW_STARTED,
        PLAN_CREATED,
        VALIDATION_COMPLETE,
        STEP_STARTED,
        STEP_COMPLETED,
        STEP_FAILED,
        REPLAN_TRIGGERED,
        WORKFLOW_COMPLETE,
        ERROR
    }

    public static WorkflowEvent of(EventType type, String node, String message) {
        return WorkflowEvent.builder()
                .eventType(type)
                .node(node)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static WorkflowEvent of(EventType type, String node, String message, Object data) {
        return WorkflowEvent.builder()
                .eventType(type)
                .node(node)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
