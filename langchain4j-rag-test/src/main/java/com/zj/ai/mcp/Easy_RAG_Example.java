package com.zj.ai.mcp;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaChatRequestParameters;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Duration;
import java.util.List;

import static com.zj.ai.mcp.Utils.*;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

public class Easy_RAG_Example {

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

        // First, let's load documents that we want to use for RAG
        List<Document> documents = loadDocuments(toPath("documents/"), glob("*.txt"));

        // Second, let's create an assistant that will have access to our documents
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(CHAT_MODEL) // it should use OpenAI LLM
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // it should remember 10 latest messages
                .contentRetriever(createContentRetriever(documents)) // it should have access to our documents
                .build();

        // Lastly, let's start the conversation with the assistant. We can ask questions like:
        // - Can I cancel my reservation?
        // - I had an accident, should I pay extra?
        startConversationWith(assistant);
    }

    private static ContentRetriever createContentRetriever(List<Document> documents) {

        // Here, we create an empty in-memory store for our documents and their embeddings.
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // Here, we are ingesting our documents into the store.
        // Under the hood, a lot of "magic" is happening, but we can ignore it for now.
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

        // Lastly, let's create a content retriever from an embedding store.
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }
}
