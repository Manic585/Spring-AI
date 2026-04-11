package com.practice.springAI.controller;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AudioController {

    private OpenAiAudioTranscriptionModel audioModel;
    private OpenAiAudioSpeechModel speechModel;

    public AudioController(OpenAiAudioTranscriptionModel audioModel, OpenAiAudioSpeechModel speechModel){
        this.audioModel = audioModel;
        this.speechModel = speechModel;
    }

    @PostMapping("/api/speechToText")
    public String speechToText(@RequestParam MultipartFile file){

        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions
                .builder()
                //.language("hi")
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.SRT)
                .build();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(file.getResource(), options);
        return audioModel.call(prompt).getResult().getOutput();
    }

    @PostMapping("/api/textToSpeech")
    public byte[] textToSpeech(@RequestParam String text){

        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions
                .builder()
                .speed(0.75f)
                .voice("nova")
                .build();
        SpeechPrompt prompt = new SpeechPrompt(text, options);

        return speechModel.call(prompt).getResult().getOutput();
    }
}
