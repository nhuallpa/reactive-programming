package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import com.reactivespring.moviesinfoservice.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {

    public static final String V_1_MOVIESINFO = "/v1/movieinfos";
    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieinfos)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {

        var moviesInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient.post()
                .uri(V_1_MOVIESINFO)
                .bodyValue(moviesInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo !=null;
                    assert savedMovieInfo.getMovieInfoId() !=null;
                });
    }

    @Test
    void getAllMoviesInfos() {
        webTestClient.get()
                .uri(V_1_MOVIESINFO)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMoviesInfosByYear() {

        var uri = UriComponentsBuilder.fromUriString(V_1_MOVIESINFO)
                        .queryParam("year", 2005)
                                .buildAndExpand().toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getAllMoviesInfoById() {
        var movieInfoId = "abc";
        webTestClient.get()
                .uri(V_1_MOVIESINFO + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
//                .expectBody(MovieInfo.class)
//                .consumeWith(movieInfoEntityExchangeResult -> {
//                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
//                    assertNotNull(movieInfo);
//                });
    }

    @Test
    void getAllMoviesInfoById_notfound() {
        var movieInfoId = "bvv";
        webTestClient.get()
                .uri(V_1_MOVIESINFO + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void updateMovieInfo() {

        var moviesInfo = new MovieInfo(null, "Dark  Knight Rises1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient.put()
                .uri(V_1_MOVIESINFO + "/{id}", "abc")
                .bodyValue(moviesInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updateddMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert updateddMovieInfo !=null;
                    assert updateddMovieInfo.getMovieInfoId() !=null;
                    assertEquals("Dark  Knight Rises1", updateddMovieInfo.getName());
                });
    }

    @Test
    void updateMovieInfo_notfound() {

        var moviesInfo = new MovieInfo(null, "Dark  Knight Rises1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient.put()
                .uri(V_1_MOVIESINFO + "/{id}", "bvv")
                .bodyValue(moviesInfo)
                .exchange()
                .expectStatus()
                .isNotFound();

    }

    @Test
    void deleteMovieInfo() {

        webTestClient.delete()
                .uri(V_1_MOVIESINFO + "/{id}", "abc")
                .exchange()
                .expectStatus()
                .isNoContent();

    }
}