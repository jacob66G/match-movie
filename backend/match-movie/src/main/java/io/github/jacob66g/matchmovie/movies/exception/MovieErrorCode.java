package io.github.jacob66g.matchmovie.movies.exception;

import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MovieErrorCode implements ErrorCode {
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "error.movie.not.found");

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
