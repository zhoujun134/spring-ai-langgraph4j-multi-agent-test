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
    private final static String baseUrl = DotEnvUtils.getDotEnvValue("CHAT_BASE_URL");
    private final static String chatModel = DotEnvUtils.getDotEnvValue("CHAT_MODEL");
    private final static String apiKey = DotEnvUtils.getDotEnvValue("CHAT_API_KEY");
    private static final String logRequestString = DotEnvUtils.getDotEnvValueOrDefault("LANGCHAIN_CHAT_LOG_REQUEST", "false");
    private static final String logResponseString = DotEnvUtils.getDotEnvValueOrDefault("LANGCHAIN_CHAT_LOG_RESPONSE", "false");
    private static final boolean logRequest = BooleanUtils.toBoolean(logRequestString);
    private static final boolean logResponse = BooleanUtils.toBoolean(logResponseString);

    public static ChatModel loadSyncOpenaiChatModel() {
        log.info("系统环境变量配置为: baseUrl:{}, chatModel:{}, apiKey:{}," + "logRequest:{}, logResponse: {}", baseUrl, chatModel, apiKey != null ? "***" : null, logRequest, logResponse);
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(chatModel)
                .baseUrl(baseUrl)
                .timeout(Duration.ofMinutes(10))
                .logRequests(logRequest)
                .logResponses(logResponse)
                .maxRetries(3)
                .temperature(0.95)
                .maxTokens(100000)
                .build();
    }

    public static StreamingChatModel loadAsyncOpenaiChatModel() {
        log.info("系统环境变量配置为: baseUrl:{}, chatModel:{}, apiKey:{}," + "logRequest:{}, logResponse: {}", baseUrl, chatModel, apiKey != null ? "***" : null, logRequest, logResponse);
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(chatModel)
                .baseUrl(baseUrl)
                .timeout(Duration.ofMinutes(10))
                .logRequests(logRequest)
                .logResponses(logResponse)
                .temperature(0.95)
                .maxTokens(100000)
                .build();
    }

    public static StreamingChatModel loadAsyncOllamaChatModel() {
        log.info("系统环境变量配置为: baseUrl:{}, chatModel:{}, apiKey:{}," + "logRequest:{}, logResponse: {}", baseUrl, chatModel, apiKey != null ? "***" : null, logRequest, logResponse);
        return OllamaStreamingChatModel.builder()
                .modelName(chatModel)
                .baseUrl(baseUrl)
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
