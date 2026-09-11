package io.github.jacob66g.matchmovie.movies.dto;

import io.github.jacob66g.matchmovie.movies.model.Movie;

import java.math.BigDecimal;
import java.time.LocalDate;


public record MovieSearchResponse(
        Long id,
        String title,
        String posterPath,
        LocalDate releaseDate,
        BigDecimal voteAverage,
        boolean inCatalog
) {

    public static MovieSearchResponse fromCatalog(Movie movie) {
        return new MovieSearchResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getPosterPath(),
                movie.getReleaseDate(),
                movie.getVoteAverage(),
                true
        );
    }
}
