package com.reactivespring.controller;

import com.reactivespring.client.MoviesInfoRestClient;
import com.reactivespring.client.ReviewRestClient;
import com.reactivespring.domain.Movie;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
@AllArgsConstructor
public class MoviesController {

    private final MoviesInfoRestClient moviesInfoRestClient;
    private final ReviewRestClient reviewRestClient;

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String movieId) {
        return moviesInfoRestClient.retrieveMovieInfo(movieId)
                .flatMap( movieInfo -> {
                    var reviewListMono = reviewRestClient.retrieveReviews(movieId)
                            .collectList();

                    return reviewListMono.map(reviews -> new Movie(movieInfo, reviews));
                });
    }
}
