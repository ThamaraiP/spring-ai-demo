package org.example;

import java.util.List;
import org.example.tools.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

  @Bean
  public VectorStore vectorStore(EmbeddingModel embeddingModel) {
    return SimpleVectorStore.builder(embeddingModel).build();
  }

  @Bean
  public ChatClient buildChatClient(Builder builder, VectorStore vectorStore,
      ChatMemory chatMemory, WeatherTools weatherTools) {
    return
        builder
            .defaultTools(weatherTools)
            .defaultAdvisors(

                // Absolutely don't let people ask about PHP 😆
                new SafeGuardAdvisor(List.of("PHP")),

                // Remember the conversation
                MessageChatMemoryAdvisor.builder(chatMemory).build(),

                // Define RAG pipeline
                // See
                // https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html#modules
                RetrievalAugmentationAdvisor.builder()
                    .queryTransformers(
                        // Rewrite the query for better search results
                        RewriteQueryTransformer.builder()
                            .chatClientBuilder(builder.build().mutate())
                            .build())
                    // Allow empty context (so you can try the assistant without context and
                    // compare)
                    .queryAugmenter(
                        ContextualQueryAugmenter.builder().allowEmptyContext(true).build())

                    // Use the vector store to retrieve documents
                    .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                            .similarityThreshold(0.50)
                            .vectorStore(vectorStore)
                            .build())
                    .build())
            .build();
  }

}
