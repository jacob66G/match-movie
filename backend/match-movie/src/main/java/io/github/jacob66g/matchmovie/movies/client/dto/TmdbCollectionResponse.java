package io.github.jacob66g.matchmovie.movies.client.dto;

public record TmdbCollectionResponse(
        Long id,
        String name,
        String posterPath,
        String backdropPath
) {
}
