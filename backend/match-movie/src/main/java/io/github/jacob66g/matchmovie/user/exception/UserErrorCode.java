package io.github.jacob66g.matchmovie.user.exception;

import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "error.user.not.found"),
    MISSING_SUB_CLAIM(HttpStatus.UNAUTHORIZED, "error.authentication.missing.claim"),
    INVALID_SUB_FORMAT(HttpStatus.UNAUTHORIZED, "error.authentication.invalid.sub.format"),
    MISSING_CLAIM_ON_PROVISION(HttpStatus.UNAUTHORIZED, "error.authentication.missing.claim");

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
