package io.github.jacob66g.matchmovie.watchlist.exception;

import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum WatchlistErrorCode implements ErrorCode {
    WATCHLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "WATCHLIST_ITEM_NOT_FOUND",
            "The movie is not on the user's watchlist."),
    ALREADY_IN_WATCHLIST(HttpStatus.CONFLICT, "WATCHLIST_ALREADY_ADDED",
            "The movie is already on the user's watchlist.");

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
