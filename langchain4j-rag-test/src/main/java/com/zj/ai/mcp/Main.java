//package com.zj.ai.mcp;
//
//import dev.langchain4j.data.document.Document;
//import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.rag.content.retriever.ContentRetriever;
//import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
//import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
//import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.ollama.OllamaEmbeddingModel;
//import org.springframework.ai.ollama.api.OllamaApi;
//import org.springframework.ai.ollama.api.OllamaOptions;
//
//import java.util.List;
//
///**
// * @ClassName ${NAME}
// * @Author zj
// * @Description
// * @Date 2026/3/18 23:18
// * @Version v1.0
// **/
//public class Main {
//    public static void main(String[] args) {
//        System.out.println("Hello world!");
//        // 从一个文件路径下加载部分文档
//        List<Document> documents = FileSystemDocumentLoader.loadDocuments("/home/langchain4j/documentation");
//        // 创建一个嵌入存储
//        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
//        EmbeddingStoreIngestor.ingest(documents, embeddingStore);
//        OllamaApi ollamaApi = OllamaApi.builder()
//                .baseUrl("")
//                .build();
//        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
//                .ollamaApi(ollamaApi)
//                .defaultOptions(OllamaOptions.builder()
//                        .model("bge-m3")
//                        .build())
//                .build();
//
//        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
//                .embeddingStore(embeddingStore)
//                .embeddingModel(embeddingModel)
//                .maxResults(3)
//                // maxResults 也可以根据查询动态指定
//                .dynamicMaxResults(query -> 3)
//                .minScore(0.75)
//                // minScore 也可以根据查询动态指定
//                .dynamicMinScore(query -> 0.75)
//                .filter(metadataKey("userId").isEqualTo("12345"))
//                // filter 也可以根据查询动态指定
//                .dynamicFilter(query -> {
//                    String userId = getUserId(query.metadata().chatMemoryId());
//                    return metadataKey("userId").isEqualTo(userId);
//                })
//                .build();
//
//
//
//    }
//}
