package io.github.jacob66g.matchmovie.movies.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbTranslationItemResponse(
        @JsonProperty("iso_3166_1") String iso31661,
        @JsonProperty("iso_639_1") String iso6391,
        String name,
        String englishName,
        TmdbTranslationDataResponse data
) {
}
