package io.github.jacob66g.matchmovie.movies.dto;

import io.github.jacob66g.matchmovie.movies.model.Movie;

import java.math.BigDecimal;
import java.time.LocalDate;


public record MovieSummaryResponse(
        Long id,
        String title,
        String posterPath,
        LocalDate releaseDate,
        Integer runtime,
        BigDecimal voteAverage
) {

    public static MovieSummaryResponse from(Movie movie) {
        return new MovieSummaryResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getPosterPath(),
                movie.getReleaseDate(),
                movie.getRuntime(),
                movie.getVoteAverage()
        );
    }
}
