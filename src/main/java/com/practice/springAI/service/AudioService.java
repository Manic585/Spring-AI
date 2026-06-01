package com.practice.springAI.service;

import com.practice.springAI.exception.SpringAiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class AudioService {

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final OpenAiAudioSpeechModel speechModel;

    public AudioService(OpenAiAudioTranscriptionModel transcriptionModel, OpenAiAudioSpeechModel speechModel) {
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
    }

    public String transcribe(MultipartFile file) {
        log.info("Transcribing audio file={}, size={}bytes", file.getOriginalFilename(), file.getSize());
        try {
            OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                    .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.SRT)
                    .build();
            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(file.getResource(), options);
            return transcriptionModel.call(prompt).getResult().getOutput();
        } catch (Exception ex) {
            log.error("Audio transcription failed for file={}", file.getOriginalFilename(), ex);
            throw new SpringAiException("Failed to transcribe audio", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    public byte[] synthesize(String text) {
        log.info("Synthesizing speech for textLength={}", text.length());
        try {
            OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                    .speed(0.75f)
                    .voice("nova")
                    .build();
            return speechModel.call(new SpeechPrompt(text, options)).getResult().getOutput();
        } catch (Exception ex) {
            log.error("Speech synthesis failed", ex);
            throw new SpringAiException("Failed to synthesize speech", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }
}
