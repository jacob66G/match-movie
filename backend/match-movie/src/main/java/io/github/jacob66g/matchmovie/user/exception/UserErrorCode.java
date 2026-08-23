package io.github.jacob66g.matchmovie.user.exception;

import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
            "No user exists for the given identifier."),
    MISSING_SUB_CLAIM(HttpStatus.UNAUTHORIZED, "USER_INVALID_TOKEN",
            "The access token is not usable for user provisioning."),
    INVALID_SUB_FORMAT(HttpStatus.UNAUTHORIZED, "USER_INVALID_TOKEN",
            "The access token is not usable for user provisioning."),
    MISSING_CLAIM_ON_PROVISION(HttpStatus.UNAUTHORIZED, "USER_INVALID_TOKEN",
            "The access token is not usable for user provisioning.");

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
