package com.practice.springAI.controller;

import com.practice.springAI.model.Movie;
import com.practice.springAI.dto.ApiResponse;
import com.practice.springAI.service.MovieService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/movies")
    public ResponseEntity<ApiResponse<List<String>>> getMovies(
            @RequestParam @NotBlank @Size(max = 100) String actor) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getMovieTitles(actor)));
    }

    @GetMapping("/movieJson")
    public ResponseEntity<ApiResponse<Movie>> getMovieJson(
            @RequestParam @NotBlank @Size(max = 100) String actor) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getBestMovie(actor)));
    }

    @GetMapping("/moviesList")
    public ResponseEntity<ApiResponse<List<Movie>>> getMoviesList(
            @RequestParam @NotBlank @Size(max = 100) String actor) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getMoviesList(actor)));
    }
}
