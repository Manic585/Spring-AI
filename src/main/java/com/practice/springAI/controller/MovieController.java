package com.practice.springAI.controller;

import com.practice.springAI.Movie;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MovieController {

    private ChatClient chatClient;

    public MovieController(@Qualifier("openAiChatModel") OpenAiChatModel openAiChatModel){
        this.chatClient = ChatClient.create(openAiChatModel);
    }

    @GetMapping("/movie/movies")
    public List<String> getMovies(@RequestParam String actor){
        String message = """
                    List top 5 movies of {actor} in 
                    {format} format
                """;
        ListOutputConverter converter = new ListOutputConverter(new DefaultConversionService());
        PromptTemplate template =
                new PromptTemplate(message);
        Prompt prompt = template.create(Map.of(
                "actor", actor,
                "format", converter.getFormat()
        ));
        List<String> movies = converter.convert(chatClient.prompt(prompt).call().content());
        return movies;
    }

    @GetMapping("/movie/movieJson")
    public Movie getMovieJson(@RequestParam String actor){
        String message = """
                    Get me the best movie of {actor} in 
                    {format} format
                """;
        BeanOutputConverter<Movie> converter = new BeanOutputConverter<>(Movie.class);
        PromptTemplate template =
                new PromptTemplate(message);
        Prompt prompt = template.create(Map.of(
                "actor", actor,
                "format", converter.getFormat()
        ));
        Movie movie = converter.convert(chatClient.prompt(prompt).call().content());
        return movie;
    }

    @GetMapping("/movie/moviesList")
    public List<Movie> getMoviesList(@RequestParam String actor){
        String message = """
                    Get me the list of movies of {actor} in 
                    {format} format
                """;


        BeanOutputConverter<List<Movie>> converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<Movie>>() {}

        );
        PromptTemplate template =
                new PromptTemplate(message);
        Prompt prompt = template.create(Map.of(
                "actor", actor,
                "format", converter.getFormat()
        ));
        List<Movie> movies = converter.convert(chatClient.prompt(prompt).call().content());
        return movies;
    }

}
