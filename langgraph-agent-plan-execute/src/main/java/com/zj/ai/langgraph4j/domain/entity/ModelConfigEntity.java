package com.zj.ai.langgraph4j.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模型配置实体
 * 用于存储 AI 模型的动态配置信息
 *
 * @author zj
 * @date 2026/04/12
 */
@Entity
@Table(name = "model_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模型标识符，如 "ollama-gemma4"
     */
    @Column(unique = true, nullable = false, length = 100)
    private String modelName;

    /**
     * 提供商: "ollama", "openai", "deepseek"
     */
    @Column(nullable = false, length = 50)
    private String provider;

    /**
     * API 基础 URL
     */
    @Column(length = 500)
    private String baseUrl;

    /**
     * API Key
     */
    @Column(length = 500)
    private String apiKey;

    /**
     * 实际模型 ID，如 "gemma4:e4b"
     */
    @Column(nullable = false, length = 100)
    private String modelId;

    /**
     * 温度参数
     */
    private Double temperature = 0.7;

    /**
     * 最大 token 数
     */
    private Integer maxTokens = 4096;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 是否默认模型
     */
    @Column(nullable = false)
    private Boolean isDefault = false;

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
