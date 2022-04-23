package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import com.reactivespring.moviesinfoservice.service.MoviesInfoService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MovieInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoService;

    public static final String V_1_MOVIESINFO = "/v1/movieinfos";


    @Test
    void getAllMovieInfo() {

        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));


        when(moviesInfoService.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieinfos));

        webTestClient.get()
                .uri(V_1_MOVIESINFO)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {

        var movieInfo = new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoService.getAllMovieInfoById(anyString())).thenReturn(Mono.just(movieInfo));

        webTestClient.get()
                .uri(V_1_MOVIESINFO + "/{id}", "abc")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith( movieInfoEntityExchangeResult -> {
                    var movieInfo1 = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieInfo1);
                });
    }


    @Test
    void addMovieInfo() {

        var moviesInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoService.addMovieInfo(isA(MovieInfo.class))).thenReturn(
                Mono.just(new MovieInfo("MockId", "Batman Begins1",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
        );

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
                    assertEquals("MockId", savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo_validation() {

        var moviesInfo = new MovieInfo(null, null,
                -2005, List.of(""), LocalDate.parse("2005-06-15"));


        webTestClient.post()
                .uri(V_1_MOVIESINFO)
                .bodyValue(moviesInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    System.out.println("responseBody : "+ responseBody);
                    var expectedErrorMessage = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be positive";
                    assert responseBody != null;
                    assertEquals(expectedErrorMessage, responseBody);
                });
    }

    @Test
    void updateMovieInfo() {

        var moviesInfo = new MovieInfo(null, "Dark  Knight Rises1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoService.updateMovieInfo(isA(MovieInfo.class), anyString())).thenReturn(
          Mono.just(new MovieInfo("abc", "Dark  Knight Rises1",
                  2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
        );

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
    void deleteMovieInfo() {

        when(moviesInfoService.deleteMovieInfo(anyString())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(V_1_MOVIESINFO + "/{id}", "abc")
                .exchange()
                .expectStatus()
                .isNoContent();

    }
}
