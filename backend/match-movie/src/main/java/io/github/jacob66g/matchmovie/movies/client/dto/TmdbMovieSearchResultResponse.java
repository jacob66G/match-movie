package io.github.jacob66g.matchmovie.movies.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TmdbMovieSearchResultResponse(
        Long id,
        boolean adult,
        boolean video,
        String backdropPath,
        String posterPath,
        String title,
        String originalTitle,
        String originalLanguage,
        String overview,
        LocalDate releaseDate,
        Double popularity,
        BigDecimal voteAverage,
        Integer voteCount,
        List<Long> genreIds
) {
}
