package io.github.jacob66g.matchmovie.movies.client.dto;

import java.util.List;

public record TmdbSearchResponse(
        int page,
        List<TmdbMovieSearchResultResponse> results,
        int totalPages,
        int totalResults
) {
}
