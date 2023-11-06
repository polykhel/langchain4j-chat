package com.example.application.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.time.Duration;

import static dev.langchain4j.data.document.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;

@Configuration
public class LangChainConfig {

    @Bean
    public HelpfulAIDeveloper helpfulAIDeveloper(StreamingChatLanguageModel chatLanguageModel, Retriever<TextSegment> retriever) {
        return AiServices.builder(HelpfulAIDeveloper.class)
                .streamingChatLanguageModel(chatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(5))
                .retriever(retriever)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel(@Value("${openai.api-key}") String apiKey) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-3.5-turbo")
                .timeout(Duration.ofMinutes(1))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public Retriever<TextSegment> retriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        return EmbeddingStoreRetriever.from(embeddingStore, embeddingModel, 1, 0.6);
    }

    @Bean
    @ConditionalOnProperty(prefix = "store", name = "populate", havingValue = "true")
    public CommandLineRunner embedStore(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, ResourceLoader resourceLoader) {
        return args -> {
            Resource resource = resourceLoader.getResource("classpath:langchain4j.pdf");
            Document document = loadDocument(resource.getFile().getPath());

            DocumentSplitter documentSplitter = DocumentSplitters.recursive(200, 0, new OpenAiTokenizer(GPT_3_5_TURBO));
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(documentSplitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            ingestor.ingest(document);
        };
    }
}
