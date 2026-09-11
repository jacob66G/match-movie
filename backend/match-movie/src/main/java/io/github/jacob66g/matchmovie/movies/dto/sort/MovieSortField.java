package io.github.jacob66g.matchmovie.movies.dto.sort;

import io.github.jacob66g.matchmovie.common.sort.SortableField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MovieSortField implements SortableField {
    TITLE("title", "title"),
    RELEASE_DATE("releaseDate", "releaseDate"),
    RUNTIME("runtime", "runtime"),
    POPULARITY("popularity", "popularity"),
    VOTE_COUNT("voteCount", "voteCount");

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
