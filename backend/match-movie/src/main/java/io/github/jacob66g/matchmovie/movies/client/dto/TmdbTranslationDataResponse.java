package io.github.jacob66g.matchmovie.movies.client.dto;

public record TmdbTranslationDataResponse(
        String title,
        String overview,
        String homepage,
        String tagline,
        Integer runtime
) {
}
