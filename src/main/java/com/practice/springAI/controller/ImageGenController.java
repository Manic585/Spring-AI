package com.practice.springAI.controller;

import com.practice.springAI.dto.ApiResponse;
import com.practice.springAI.service.ImageService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/image")
public class ImageGenController {

    private final ImageService imageService;

    public ImageGenController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{query}")
    public ResponseEntity<ApiResponse<String>> generateImage(
            @PathVariable @NotBlank @Size(max = 1000) String query) {
        return ResponseEntity.ok(ApiResponse.ok(imageService.generate(query)));
    }

    @PostMapping("/describe")
    public ResponseEntity<ApiResponse<String>> describeImage(
            @RequestParam @NotBlank @Size(max = 500) String query,
            @RequestParam MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(imageService.describe(query, file)));
    }
}
