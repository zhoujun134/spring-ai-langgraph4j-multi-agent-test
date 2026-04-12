package com.zj.ai.common;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Duration;
import java.util.List;

import static com.zj.ai.common.Utils.glob;
import static com.zj.ai.common.Utils.toPath;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

/**
 * @ClassName ${NAME}
 * @Author zj
 * @Description
 * @Date 2026/3/18 23:18
 * @Version v1.0
 **/
public class Main {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("/Users/zj/IdeaProjects/zj/spring-ai-langgraph4j-multil-agent-test/")           // 指定 .env 文件目录
            .filename(".env")          // 指定文件名
            .ignoreIfMalformed()       // 忽略格式错误
            .load();
    private static final String ollamaBaseUrl = dotenv.get("OLLAMA_BASE_URL");
    private static final String ollamaChatModel = dotenv.get("OLLAMA_CHAT_MODEL");
    private static final String ollamaEmbeddingModel = dotenv.get("OLLAMA_EMBEDDING_MODEL");
    public static void main(String[] args) {
        System.out.println("Hello world!");
        // 从一个文件路径下加载部分文档
//        List<Document> documents = FileSystemDocumentLoader.loadDocuments("/home/langchain4j/documentation");
        List<Document> documents = loadDocuments(toPath("documents/"), glob("*.txt"));
        // 创建一个嵌入存储
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor.ingest(documents, embeddingStore);
        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaEmbeddingModel)
                .timeout(Duration.ofSeconds(1000))
                .build();

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                // maxResults 也可以根据查询动态指定
                .dynamicMaxResults(query -> 3)
                .minScore(0.75)
                // minScore 也可以根据查询动态指定
                .dynamicMinScore(query -> 0.75)
//                .filter(metadataKey("userId").isEqualTo("12345"))
                // filter 也可以根据查询动态指定
//                .dynamicFilter(query -> {
//                    String userId = getUserId(query.metadata().chatMemoryId());
//                    return metadataKey("userId").isEqualTo(userId);
//                })
                .build();
        // 进行查询
        List<Content> queryResult = contentRetriever.retrieve(Query.from("这一成就使他获得了声望大学"));
        System.out.println(queryResult);

    }
}
