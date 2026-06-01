package com.practice.springAI.service;

import com.practice.springAI.exception.SpringAiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(@Qualifier("openAiChatModel") OpenAiChatModel model) {
        this.chatClient = ChatClient.create(model);
    }

    public String chat(String message) {
        log.info("Processing chat request, messageLength={}", message.length());
        try {
            return chatClient.prompt(message)
                    .call()
                    .chatResponse()
                    .getResult()
                    .getOutput()
                    .getText();
        } catch (Exception ex) {
            log.error("Chat request failed", ex);
            throw new SpringAiException("Failed to process chat request", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    public String recommend(String type, String year, String language) {
        log.info("Fetching movie recommendations: type={}, year={}, language={}", type, year, language);
        String template = """
                I want to watch {type} movie in {language} which got released in the {year}.
                Suggest 5 movies for the requirement and give me the following in numbered list.
                1. Cast
                2. Length time
                3. IMDB Ratings
                """;
        try {
            PromptTemplate promptTemplate = new PromptTemplate(template);
            Prompt prompt = promptTemplate.create(Map.of("type", type, "year", year, "language", language));
            return chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getText();
        } catch (Exception ex) {
            log.error("Movie recommendation failed", ex);
            throw new SpringAiException("Failed to fetch recommendations", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }
}
