package com.zj.ai.rag;

import dev.langchain4j.service.SystemMessage;

public interface Assistant {
    @SystemMessage("请使用中文回答问题")
    String answer(String query);
}
