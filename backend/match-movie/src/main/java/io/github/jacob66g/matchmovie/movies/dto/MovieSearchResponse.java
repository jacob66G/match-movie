package io.github.jacob66g.matchmovie.movies.dto;

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
}
