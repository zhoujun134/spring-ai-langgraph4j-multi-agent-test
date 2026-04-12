package com.zj.ai.langgraph4j.service;

import com.zj.ai.langgraph4j.domain.entity.ModelConfigEntity;
import com.zj.ai.langgraph4j.repository.ModelConfigRepository;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态模型管理器
 * 支持运行时动态加载和切换模型
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Service
public class DynamicModelManager {

    private final ModelConfigRepository repository;

    /**
     * 模型缓存
     */
    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<String, StreamingChatModel> streamingModelCache = new ConcurrentHashMap<>();

    public DynamicModelManager(ModelConfigRepository repository) {
        this.repository = repository;
    }

    // ==================== 获取模型 ====================

    /**
     * 获取默认聊天模型
     */
    public ChatModel getDefaultChatModel() {
        ModelConfigEntity config = repository.findByIsDefaultTrue()
                .orElseThrow(() -> new RuntimeException("No default model configured"));
        return getChatModel(config.getModelName());
    }

    /**
     * 获取默认流式模型
     */
    public StreamingChatModel getDefaultStreamingModel() {
        ModelConfigEntity config = repository.findByIsDefaultTrue()
                .orElseThrow(() -> new RuntimeException("No default model configured"));
        return getStreamingModel(config.getModelName());
    }

    /**
     * 根据名称获取聊天模型（懒加载 + 缓存）
     */
    public ChatModel getChatModel(String modelName) {
        return chatModelCache.computeIfAbsent(modelName, name -> {
            ModelConfigEntity config = repository.findByModelName(name)
                    .orElseThrow(() -> new RuntimeException("Model not found: " + name));
            return createChatModel(config);
        });
    }

    /**
     * 根据名称获取流式模型（懒加载 + 缓存）
     */
    public StreamingChatModel getStreamingModel(String modelName) {
        return streamingModelCache.computeIfAbsent(modelName, name -> {
            ModelConfigEntity config = repository.findByModelName(name)
                    .orElseThrow(() -> new RuntimeException("Model not found: " + name));
            return createStreamingModel(config);
        });
    }

    // ==================== 创建模型 ====================

    /**
     * 创建聊天模型实例
     */
    private ChatModel createChatModel(ModelConfigEntity config) {
        log.info("Creating chat model: {} with provider: {}", config.getModelName(), config.getProvider());

        return switch (config.getProvider().toLowerCase()) {
            case "ollama" -> createOllamaChatModel(config);
            case "openai" -> createOpenAiChatModel(config);
            default -> throw new RuntimeException("Unknown provider: " + config.getProvider());
        };
    }

    /**
     * 创建流式模型实例
     */
    private StreamingChatModel createStreamingModel(ModelConfigEntity config) {
        log.info("Creating streaming model: {} with provider: {}", config.getModelName(), config.getProvider());

        return switch (config.getProvider().toLowerCase()) {
            case "ollama" -> createOllamaStreamingModel(config);
            case "openai" -> createOpenAiStreamingModel(config);
            default -> throw new RuntimeException("Unknown provider: " + config.getProvider());
        };
    }

    // ==================== Ollama 模型创建 ====================

    private OllamaChatModel createOllamaChatModel(ModelConfigEntity config) {
        return OllamaChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelId())
                .temperature(config.getTemperature() != null ? config.getTemperature() : 0.7)
                .timeout(Duration.ofMinutes(10))
                .build();
    }

    private OllamaStreamingChatModel createOllamaStreamingModel(ModelConfigEntity config) {
        return OllamaStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelId())
                .temperature(config.getTemperature() != null ? config.getTemperature() : 0.7)
                .timeout(Duration.ofMinutes(10))
                .build();
    }

    // ==================== OpenAI 兼容模型创建 ====================

    private OpenAiChatModel createOpenAiChatModel(ModelConfigEntity config) {
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl() != null ? config.getBaseUrl() + "/v1" : null)
                .apiKey(config.getApiKey())
                .modelName(config.getModelId())
                .temperature(config.getTemperature() != null ? config.getTemperature() : 0.7)
                .maxTokens(config.getMaxTokens() != null ? config.getMaxTokens() : 4096)
                .timeout(Duration.ofMinutes(10))
                .build();
    }

    private OpenAiStreamingChatModel createOpenAiStreamingModel(ModelConfigEntity config) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl() != null ? config.getBaseUrl() + "/v1" : null)
                .apiKey(config.getApiKey())
                .modelName(config.getModelId())
                .temperature(config.getTemperature() != null ? config.getTemperature() : 0.7)
                .maxTokens(config.getMaxTokens() != null ? config.getMaxTokens() : 4096)
                .timeout(Duration.ofMinutes(10))
                .build();
    }

    // ==================== 缓存管理 ====================

    /**
     * 刷新指定模型的缓存
     */
    public void refreshModel(String modelName) {
        chatModelCache.remove(modelName);
        streamingModelCache.remove(modelName);
        log.info("Model cache refreshed: {}", modelName);
    }

    /**
     * 清空所有模型缓存
     */
    public void clearCache() {
        chatModelCache.clear();
        streamingModelCache.clear();
        log.info("All model caches cleared");
    }

    // ==================== 配置管理 ====================

    /**
     * 获取所有启用的模型配置
     */
    public List<ModelConfigEntity> getEnabledModels() {
        return repository.findByEnabledTrue();
    }

    /**
     * 获取模型配置
     */
    public Optional<ModelConfigEntity> getModelConfig(String modelName) {
        return repository.findByModelName(modelName);
    }

    /**
     * 保存模型配置
     */
    public ModelConfigEntity saveModelConfig(ModelConfigEntity config) {
        ModelConfigEntity saved = repository.save(config);
        // 刷新缓存
        refreshModel(config.getModelName());
        return saved;
    }
}
