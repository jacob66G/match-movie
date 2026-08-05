package io.github.jacob66g.matchmovie.watchlist.exception;

import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum WatchlistErrorCode implements ErrorCode {
    WATCHLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "error.watchlist.item.not.found"),
    ALREADY_IN_WATCHLIST(HttpStatus.CONFLICT, "error.watchlist.already.added");

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
