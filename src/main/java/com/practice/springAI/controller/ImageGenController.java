package com.practice.springAI.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.function.Consumer;

@RestController
public class ImageGenController {

    private ChatClient chatClient;

    private OpenAiImageModel openAiImageModel;

    public ImageGenController(OpenAiImageModel openAiImageModel, ChatClient.Builder builder){
        this.openAiImageModel = openAiImageModel;
        chatClient = builder.build();
    }

    @GetMapping("image/{query}")
    public String generateImage(@PathVariable String query){

        ImagePrompt imagePrompt = new ImagePrompt(query, OpenAiImageOptions
                .builder()
                .quality("hd")
                .height(1024)
                .style("natural")
                .build());

        ImageResponse imageResponse = openAiImageModel.call(imagePrompt);

        return imageResponse.getResult().getOutput().getUrl();
    }

    @PostMapping("image/describe")
    public String describeImage(@RequestParam String query, @RequestParam MultipartFile file) throws IOException {

        System.out.println(file.getOriginalFilename());
        System.out.println(query);

        Consumer<ChatClient.PromptUserSpec> userConsumer = userSpec -> {
            userSpec.text(query)
                    .media(MimeTypeUtils.IMAGE_JPEG, file.getResource());
        };

        return chatClient
                .prompt()
                .user(userConsumer)
                .call()
                .content();
    }
}
