package io.github.jacob66g.matchmovie.watchlist.sort;

import io.github.jacob66g.matchmovie.common.sort.SortableField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum WatchedMovieSortField implements SortableField {

    WATCHED_AT("watchedAt", "watchedAt"),
    RATING("rating", "rating"),
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
