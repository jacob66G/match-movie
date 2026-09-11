package io.github.jacob66g.matchmovie.watchlist.dto.sort;

import io.github.jacob66g.matchmovie.common.sort.SortableField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum WatchlistSortField implements SortableField {

    ADDED_AT("addedAt", "addedAt"),
    TITLE("title", "movie.title"),
    RELEASE_DATE("releaseDate", "movie.releaseDate"),
    MOVIE_ID("movieId", "id.movieId");

    private final String apiName;
    private final String entityPath;

    @Override
    public String apiName() {
        return apiName;
    }

    @Override
    public String entityPath() {
        return entityPath;
    }
}
