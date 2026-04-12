package com.zj.ai.langgraph4j.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工具配置实体
 * 用于存储动态工具的配置信息
 *
 * @author zj
 * @date 2026/04/12
 */
@Entity
@Table(name = "tool_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 工具名称，如 "calculator"
     */
    @Column(unique = true, nullable = false, length = 100)
    private String toolName;

    /**
     * 工具描述
     */
    @Column(length = 1000)
    private String description;

    /**
     * 实现类全限定名
     */
    @Column(nullable = false, length = 500)
    private String className;

    /**
     * 方法名
     */
    @Column(length = 100)
    private String methodName;

    /**
     * 参数定义 (JSON 格式)
     */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 优先级
     */
    private Integer priority = 0;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
