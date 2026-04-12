package com.zj.ai.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.zj.ai.rag.Assistant;
import dev.langchain4j.community.browser.playwright.PlaywrightBrowserExecutionEngine;
import dev.langchain4j.community.tool.browseruse.BrowserUseTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaChatRequestParameters;
import dev.langchain4j.service.AiServices;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Duration;

/**
 * @ClassName TestPlaywright
 * @Author zj
 * @Description
 * @Date 2026/4/4 20:13
 * @Version v1.0
 **/
public class TestPlaywright {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./")           // 指定 .env 文件目录
            .filename(".env")          // 指定文件名
            .ignoreIfMalformed()       // 忽略格式错误
            .load();
    private static final String ollamaBaseUrl = dotenv.get("OLLAMA_BASE_URL");
    private static final String ollamaChatModel = dotenv.get("OLLAMA_CHAT_MODEL");
    private static final String ollamaEmbeddingModel = dotenv.get("OLLAMA_EMBEDDING_MODEL");

    private static final OllamaChatRequestParameters qwen359bOptions = OllamaChatRequestParameters.builder()
            .topK(20)
            .topP(0.95)
            .temperature(1.0)
            .repeatPenalty(1.0)
//            .presencePenalty(1.5)
            .modelName(ollamaChatModel)
            .build();
    private static final ChatModel CHAT_MODEL = OllamaChatModel.builder()
            .baseUrl(ollamaBaseUrl)
            .modelName(ollamaChatModel)
            .timeout(Duration.ofMinutes(10))
            .defaultRequestParameters(qwen359bOptions)
            .topK(20)
            .topP(0.95)
            .build();

    public static void main(String[] args) {
        System.out.println(ollamaBaseUrl);
        System.out.println(ollamaChatModel);
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                // 隐藏浏览器视图
                .setHeadless(true)
                .setChannel("chrome")
                .setChromiumSandbox(true)
                .setSlowMo(500);
        Browser browser = playwright.chromium().launch(options);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(CHAT_MODEL)
                .tools(BrowserUseTool.from(PlaywrightBrowserExecutionEngine.builder().browser(browser).build()))
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        String question = "open page 'https://docs.langchain4j.dev/', and summary the page text";
        System.out.println(assistant.answer(question));
    }
}
