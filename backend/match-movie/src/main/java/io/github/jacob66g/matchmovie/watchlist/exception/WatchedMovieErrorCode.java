package io.github.jacob66g.matchmovie.watchlist.exception;

import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum WatchedMovieErrorCode implements ErrorCode {
    WATCHED_MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.watched.movie.item.not.found"),
    ALREADY_IN_WATCHED_MOVIES(HttpStatus.CONFLICT, "error.watched.movie.already.added");

    private final HttpStatus status;
    private final String messageKey;

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }
}
