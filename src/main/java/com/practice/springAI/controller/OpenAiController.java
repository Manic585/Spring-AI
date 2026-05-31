package com.practice.springAI.controller;

import com.practice.springAI.dto.ApiResponse;
import com.practice.springAI.dto.SimilarityResponse;
import com.practice.springAI.service.ChatService;
import com.practice.springAI.service.RagService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
public class OpenAiController {

    private final ChatService chatService;
    private final RagService ragService;

    public OpenAiController(ChatService chatService, RagService ragService) {
        this.chatService = chatService;
        this.ragService = ragService;
    }

    @GetMapping("/chat/{message}")
    public ResponseEntity<ApiResponse<String>> getResponse(
            @PathVariable @NotBlank @Size(max = 1000) String message) {
        return ResponseEntity.ok(ApiResponse.ok(chatService.chat(message)));
    }

    @PostMapping("/chat/recommend")
    public ResponseEntity<ApiResponse<String>> getRecommendation(
            @RequestParam @NotBlank String type,
            @RequestParam @NotBlank String year,
            @RequestParam @NotBlank String language) {
        return ResponseEntity.ok(ApiResponse.ok(chatService.recommend(type, year, language)));
    }

    @PostMapping("/api/embedding")
    public ResponseEntity<ApiResponse<float[]>> getEmbeddings(
            @RequestParam @NotBlank @Size(max = 5000) String text) {
        return ResponseEntity.ok(ApiResponse.ok(ragService.embed(text)));
    }

    @PostMapping("/api/similarity")
    public ResponseEntity<ApiResponse<SimilarityResponse>> getSimilarity(
            @RequestParam @NotBlank @Size(max = 1000) String t1,
            @RequestParam @NotBlank @Size(max = 1000) String t2) {
        return ResponseEntity.ok(ApiResponse.ok(ragService.computeSimilarity(t1, t2)));
    }

    @PostMapping("/api/product")
    public ResponseEntity<ApiResponse<List<Document>>> getProducts(
            @RequestParam @NotBlank @Size(max = 500) String text) {
        return ResponseEntity.ok(ApiResponse.ok(ragService.semanticSearch(text)));
    }

    @PostMapping("/api/ask")
    public ResponseEntity<ApiResponse<String>> getUsingRag(
            @RequestParam @NotBlank @Size(max = 1000) String query) {
        return ResponseEntity.ok(ApiResponse.ok(ragService.answerWithRag(query)));
    }
}
