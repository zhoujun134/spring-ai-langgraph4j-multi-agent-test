package com.zj.ai.langgraph4j.chat;

import com.zj.ai.common.sdk.BooleanUtils;
import com.zj.ai.common.sdk.dotenv.DotEnvUtils;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * @ClassName ChatModelLoader
 * @Author zj
 * @Description
 * @Date 2026/4/12 18:46
 * @Version v1.0
 **/
@Slf4j
public class ChatModelLoader {
    private final static String ollamaBaseUrl = DotEnvUtils.getDotEnvValue("OLLAMA_BASE_URL");
    private final static  String ollamaChatModel = DotEnvUtils.getDotEnvValue("OLLAMA_CHAT_MODEL");
    private final static  String ollamaApiKey = DotEnvUtils.getDotEnvValue("OLLAMA_API_KEY");
    private static final String logRequestString = DotEnvUtils.getDotEnvValueOrDefault("LANGCHAIN_CHAT_LOG_REQUEST", "false");
    private static final String logResponseString = DotEnvUtils.getDotEnvValueOrDefault("LANGCHAIN_CHAT_LOG_RESPONSE", "false");
    private static final boolean logRequest = BooleanUtils.toBoolean(logRequestString);
    private static final boolean logResponse = BooleanUtils.toBoolean(logResponseString);

    public static ChatModel loadSyncOpenaiChatModel() {
        log.info("系统环境变量配置为: ollamaBaseUrl:{}, ollamaChatModel:{}, ollamaApiKey:{}," + "logRequest:{}, logResponse: {}", ollamaBaseUrl, ollamaChatModel, ollamaApiKey, logRequest, logResponse);
        return OpenAiChatModel.builder()
                .apiKey(ollamaApiKey)
                .modelName(ollamaChatModel)
                .baseUrl(ollamaBaseUrl + "/v1")
                .timeout(Duration.ofMinutes(10))
                .logRequests(logRequest)
                .logResponses(logResponse)
                .maxRetries(3)
                .temperature(0.95)
                .maxTokens(100000)
                .build();
    }

    public static StreamingChatModel loadAsyncOpenaiChatModel() {
        log.info("系统环境变量配置为: ollamaBaseUrl:{}, ollamaChatModel:{}, ollamaApiKey:{}," + "logRequest:{}, logResponse: {}", ollamaBaseUrl, ollamaChatModel, ollamaApiKey, logRequest, logResponse);
        return OpenAiStreamingChatModel.builder()
                .apiKey(ollamaApiKey)
                .modelName(ollamaChatModel)
                .baseUrl(ollamaBaseUrl + "/v1")
                .timeout(Duration.ofMinutes(10))
                .logRequests(logRequest)
                .logResponses(logResponse)
                // 移除 returnThinking，Ollama 的 reasoning 字段处理可能有问题
                .temperature(0.95)
                .sendThinking(true)
                .returnThinking(true)
                .maxTokens(100000)
                .build();
    }

    public static StreamingChatModel loadAsyncOllamaChatModel() {
        log.info("系统环境变量配置为: ollamaBaseUrl:{}, ollamaChatModel:{}, ollamaApiKey:{}," + "logRequest:{}, logResponse: {}", ollamaBaseUrl, ollamaChatModel, ollamaApiKey, logRequest, logResponse);
        return OllamaStreamingChatModel.builder()
                .modelName(ollamaChatModel)
                .baseUrl(ollamaBaseUrl)
                .timeout(Duration.ofMinutes(10))
                .logRequests(logRequest)
                .logResponses(logResponse)
                .returnThinking(true)
                .think(true)
                .temperature(0.95)
                .numCtx(100000)
                .build();
    }

}
