package com.practice.springAI.service;

import com.practice.springAI.dto.SimilarityResponse;
import com.practice.springAI.exception.SpringAiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RagService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public RagService(
            @Qualifier("openAiChatModel") OpenAiChatModel model,
            @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel,
            VectorStore vectorStore) {
        this.chatClient = ChatClient.create(model);
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    public float[] embed(String text) {
        log.debug("Generating embedding for text of length={}", text.length());
        try {
            return embeddingModel.embed(text);
        } catch (Exception ex) {
            log.error("Embedding generation failed", ex);
            throw new SpringAiException("Failed to generate embedding", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    public SimilarityResponse computeSimilarity(String text1, String text2) {
        log.info("Computing cosine similarity between two texts");
        float[] e1 = embed(text1);
        float[] e2 = embed(text2);

        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < e1.length; i++) {
            dot   += e1[i] * e2[i];
            norm1 += e1[i] * e1[i];
            norm2 += e2[i] * e2[i];
        }
        double score = dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
        log.debug("Cosine similarity score={}", score);

        return SimilarityResponse.builder()
                .text1(text1)
                .text2(text2)
                .similarityScore(score)
                .build();
    }

    public List<Document> semanticSearch(String query) {
        log.info("Performing semantic search, query='{}'", query);
        try {
            return vectorStore.similaritySearch(query);
        } catch (Exception ex) {
            log.error("Semantic search failed", ex);
            throw new SpringAiException("Failed to perform semantic search", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    public String answerWithRag(String query) {
        log.info("Processing RAG query='{}'", query);
        try {
            return chatClient
                    .prompt(query)
                    .advisors(new QuestionAnswerAdvisor(vectorStore))
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("RAG query failed", ex);
            throw new SpringAiException("Failed to process RAG query", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }
}
