package io.github.jacob66g.matchmovie.watchlist.exception;

import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum WatchedMovieErrorCode implements ErrorCode {
    WATCHED_MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "WATCHED_MOVIE_NOT_FOUND",
            "The movie is not among the user's watched movies."),
    ALREADY_IN_WATCHED_MOVIES(HttpStatus.CONFLICT, "WATCHED_MOVIE_ALREADY_ADDED",
            "The movie is already among the user's watched movies.");

    private final HttpStatus status;
    private final String code;
    private final String detail;

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String detail() {
        return detail;
    }
}
