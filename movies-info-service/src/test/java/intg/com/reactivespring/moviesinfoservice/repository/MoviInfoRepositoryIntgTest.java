package com.reactivespring.moviesinfoservice.repository;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

@DataMongoTest
@ActiveProfiles("test")
class MoviInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    void findAll() {

        var moviesInfo = movieInfoRepository.findAll();

        StepVerifier.create(moviesInfo)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {

        var moviesInfo = movieInfoRepository.findById("abc");

        StepVerifier.create(moviesInfo)
                .assertNext( movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {

        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        var moviesInfo = movieInfoRepository.save(movieInfo);

        StepVerifier.create(moviesInfo)
                .assertNext( movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals("Batman Begins1", movieInfo1.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {

        var movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2021);

        var moviesInfo = movieInfoRepository.save(movieInfo);

        StepVerifier.create(moviesInfo)
                .assertNext( movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals(2021, movieInfo1.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {

        movieInfoRepository.deleteById("abc").block();

        var moviesInfo = movieInfoRepository.findAll();

        StepVerifier.create(moviesInfo)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByYear() {

        var moviesInfo = movieInfoRepository.findByYear(2005).log();

        StepVerifier.create(moviesInfo)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByName() {

        var moviesInfo = movieInfoRepository.findFirstByName("Batman Begins");

        StepVerifier.create(moviesInfo)
                .expectNextCount(1)
                .verifyComplete();
    }



}