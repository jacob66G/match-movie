package io.github.jacob66g.matchmovie.movies.client.dto;

import java.util.List;

public record TmdbGenreListResponse(
        List<TmdbGenreResponse> genres
) {
}
