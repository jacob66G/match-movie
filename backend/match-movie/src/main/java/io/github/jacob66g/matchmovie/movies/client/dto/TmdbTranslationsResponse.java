package io.github.jacob66g.matchmovie.movies.client.dto;

import java.util.List;

public record TmdbTranslationsResponse(
        List<TmdbTranslationItemResponse> translations
) {
}
