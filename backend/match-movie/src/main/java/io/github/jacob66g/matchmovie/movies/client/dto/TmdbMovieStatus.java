package io.github.jacob66g.matchmovie.movies.client.dto;

import lombok.Getter;

@Getter
public enum TmdbMovieStatus {
    RUMORED("Rumored"),
    PLANNED("Planned"),
    IN_PRODUCTION("In Production"),
    POST_PRODUCTION("Post Production"),
    RELEASED("Released"),
    CANCELED("Canceled");

    private final String apiValue;

    TmdbMovieStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    public boolean matches(String status) {
        return apiValue.equals(status);
    }
}
