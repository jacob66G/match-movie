package io.github.jacob66g.matchmovie.movies.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TmdbMovieDetailsResponse(
        Long id,
        boolean adult,
        boolean video,
        String backdropPath,
        String posterPath,
        String title,
        String originalTitle,
        String originalLanguage,
        String overview,
        String tagline,
        String status,
        String imdbId,
        LocalDate releaseDate,
        Integer runtime,
        Double popularity,
        BigDecimal voteAverage,
        Integer voteCount,
        TmdbCollectionResponse belongsToCollection,
        List<TmdbGenreResponse> genres,
        TmdbTranslationsResponse translations
) {
}
