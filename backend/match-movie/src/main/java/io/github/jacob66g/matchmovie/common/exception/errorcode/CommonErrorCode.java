package io.github.jacob66g.matchmovie.common.exception.errorcode;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_INTERNAL_ERROR",
            "Unexpected server error."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_UNAUTHORIZED",
            "Authentication is required to access this resource."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON_ACCESS_DENIED",
            "The authenticated user is not allowed to perform this operation."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_VALIDATION_ERROR",
            "The request contains invalid data. See fieldErrors for details."),
    INVALID_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "COMMON_INVALID_SORT_PROPERTY",
            "The requested sort property is not supported. See messageParams for allowed values.");

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
