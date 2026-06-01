package com.practice.springAI.service;

import com.practice.springAI.model.Movie;
import com.practice.springAI.exception.SpringAiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MovieService {

    private final ChatClient chatClient;

    public MovieService(@Qualifier("openAiChatModel") OpenAiChatModel model) {
        this.chatClient = ChatClient.create(model);
    }

    public List<String> getMovieTitles(String actor) {
        log.info("Fetching movie titles for actor={}", actor);
        try {
            ListOutputConverter converter = new ListOutputConverter(new DefaultConversionService());
            PromptTemplate template = new PromptTemplate(
                    "List top 5 movies of {actor} in {format} format");
            Prompt prompt = template.create(Map.of("actor", actor, "format", converter.getFormat()));
            return converter.convert(chatClient.prompt(prompt).call().content());
        } catch (Exception ex) {
            log.error("Failed to fetch movie titles for actor={}", actor, ex);
            throw new SpringAiException("Failed to retrieve movie titles", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    public Movie getBestMovie(String actor) {
        log.info("Fetching best movie for actor={}", actor);
        try {
            BeanOutputConverter<Movie> converter = new BeanOutputConverter<>(Movie.class);
            PromptTemplate template = new PromptTemplate(
                    "Get me the best movie of {actor} in {format} format");
            Prompt prompt = template.create(Map.of("actor", actor, "format", converter.getFormat()));
            return converter.convert(chatClient.prompt(prompt).call().content());
        } catch (Exception ex) {
            log.error("Failed to fetch best movie for actor={}", actor, ex);
            throw new SpringAiException("Failed to retrieve movie details", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }

    public List<Movie> getMoviesList(String actor) {
        log.info("Fetching full movie list for actor={}", actor);
        try {
            BeanOutputConverter<List<Movie>> converter = new BeanOutputConverter<>(
                    new ParameterizedTypeReference<>() {});
            PromptTemplate template = new PromptTemplate(
                    "Get me the list of movies of {actor} in {format} format");
            Prompt prompt = template.create(Map.of("actor", actor, "format", converter.getFormat()));
            return converter.convert(chatClient.prompt(prompt).call().content());
        } catch (Exception ex) {
            log.error("Failed to fetch movies list for actor={}", actor, ex);
            throw new SpringAiException("Failed to retrieve movies list", HttpStatus.SERVICE_UNAVAILABLE, ex);
        }
    }
}
