package io.github.jacob66g.matchmovie.movies.exception;

import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MovieErrorCode implements ErrorCode {
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "MOVIE_NOT_FOUND",
            "The movie is not present in the local catalog."),
    TMDB_MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "MOVIE_TMDB_NOT_FOUND",
            "The movie does not exist in TMDB."),
    TMDB_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "MOVIE_TMDB_UNAVAILABLE",
            "The TMDB provider is temporarily unavailable. Retrying later may succeed."),
    TMDB_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "MOVIE_TMDB_INVALID_RESPONSE",
            "The TMDB provider returned an incomplete or malformed response.");

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
