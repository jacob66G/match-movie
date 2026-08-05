package io.github.jacob66g.matchmovie.common.exception.errorcode;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "error.unauthorized"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "error.access.denied"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "error.validation"),
    INVALID_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "error.sort.property.invalid");

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
