package com.practice.springAI.controller;

import com.practice.springAI.dto.ApiResponse;
import com.practice.springAI.service.AudioService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api")
public class AudioController {

    private final AudioService audioService;

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping("/speechToText")
    public ResponseEntity<ApiResponse<String>> speechToText(@RequestParam MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(audioService.transcribe(file)));
    }

    @PostMapping("/textToSpeech")
    public ResponseEntity<byte[]> textToSpeech(
            @RequestParam @NotBlank @Size(max = 4096) String text) {
        byte[] audio = audioService.synthesize(text);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"speech.mp3\"")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(audio);
    }
}
