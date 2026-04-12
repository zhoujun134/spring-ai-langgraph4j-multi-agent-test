package com.zj.ai.langgraph4j;

import com.zj.ai.langgraph4j.chat.ChatModelLoader;
import com.zj.ai.langgraph4j.chat.utils.LangchainStreamChatUtils;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName ${NAME}
 * @Author zj
 * @Description
 * @Date 2026/4/12 17:20
 * @Version v1.0
 **/
@Slf4j
public class Main {

    static {
        // 禁用 Ollama 服务器的代理（解决 502 错误）
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|192.168.*|10.*|*.local");
        System.setProperty("https.nonProxyHosts", "localhost|127.0.0.1|192.168.*|10.*|*.local");
    }

    public static void main(String[] args) {
        // 使用 OpenAI 兼容接口
        final StreamingChatModel streamingChatModel = ChatModelLoader.loadAsyncOpenaiChatModel();

        String message = "你是谁？能为我做些什么？";
        LangchainStreamChatUtils.printAndResult(streamingChatModel, message);
    }
}
