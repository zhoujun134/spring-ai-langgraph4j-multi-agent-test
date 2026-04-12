package com.zj.ai.langgraph4j.chat.utils;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * @author zhoujun09
 * Created on 2026-01-13
 */
@Slf4j
public class LangchainStreamChatUtils {

    public static String printAndResult(StreamingChatModel model, String message) {
        onPartialResponseAndErrorBlocking(model, message, System.out::print,
                throwable -> log.error("流式处理失败。message: {}, error:{}", message, throwable.getMessage(), throwable));
        return "";
    }

    public static void onPartialResponseAndErrorBlocking(StreamingChatModel model,
                                                           String message,
                                                           Consumer<String> onPartialResponse,
                                                           Consumer<Throwable> onError) {
        CountDownLatch completionLatch = new CountDownLatch(1);
        try {
            StreamingChatResponseHandler handler = new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    onPartialResponse.accept(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    completionLatch.countDown();
                }

                @Override
                public void onError(Throwable error) {
                    onError.accept(error);
                    completionLatch.countDown();
                }

                @Override
                public void onPartialThinking(PartialThinking partialThinking) {
                    onPartialResponse.accept(partialThinking.text());
                }

                @Override
                public void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {
                    onPartialResponse.accept(partialThinking.text());
                }
            };
            log.info("大模型调用开始");
            long startMills = System.currentTimeMillis();
            model.chat(message, handler);
            completionLatch.await();
            long endMills = System.currentTimeMillis();
            log.info("大模型调用结束,耗时:{} ms", endMills - startMills);
        } catch (Exception exception) {
            log.error("流式处理失败。message: {}", message, exception);
        }
    }
}
