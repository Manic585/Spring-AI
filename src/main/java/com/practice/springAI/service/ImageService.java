package com.practice.springAI.service;

import com.practice.springAI.exception.SpringAiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@Service
public class ImageService {

    private final OpenAiImageModel imageModel;
    private final ChatClient chatClient;

    public ImageService(OpenAiImageModel imageModel, ChatClient.Builder builder) {
        this.imageModel = imageModel;
        this.chatClient = builder.build();
    }

    public String generate(String prompt) {
        log.info("Generating image for prompt='{}'", prompt);
        try {
            ImagePrompt imagePrompt = new ImagePrompt(prompt, OpenAiImageOptions.builder()
                    .quality("hd")
                    .height(1024)
                    .style("natural")
                    .build());
            ImageResponse response = imageModel.call(imagePrompt);
            String url = response.getResult().getOutput().getUrl();
            log.info("Image generated successfully");
            return url;
        } catch (Exception ex) {
            log.error("Image generation failed for prompt='{}'", prompt, ex);
            throw new SpringAiException("Failed to generate image", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    public String describe(String query, MultipartFile file) {
        log.info("Describing image file={}, query='{}'", file.getOriginalFilename(), query);
        try {
            return chatClient.prompt()
                    .user(u -> u.text(query).media(MimeTypeUtils.IMAGE_JPEG, file.getResource()))
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("Image description failed for file={}", file.getOriginalFilename(), ex);
            throw new SpringAiException("Failed to describe image", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }
}
