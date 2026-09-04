package io.github.jacob66g.matchmovie.movies.client.dto;

import lombok.Getter;

@Getter
public enum TmdbSortField {
    VOTE_COUNT_ASC("vote_count.asc"),
    VOTE_COUNT_DESC("vote_count.desc"),
    POPULARITY_ASC("popularity.asc"),
    POPULARITY_DESC("popularity.desc"),
    RELEASE_DATE_ASC("primary_release_date.asc"),
    RELEASE_DATE_DESC("primary_release_date.desc"),
    VOTE_AVERAGE_ASC("vote_average.asc"),
    VOTE_AVERAGE_DESC("vote_average.desc"),
    REVENUE_ASC("revenue.asc"),
    REVENUE_DESC("revenue.desc");

    private final String apiValue;

    TmdbSortField(String apiValue) {
        this.apiValue = apiValue;
    }

}
