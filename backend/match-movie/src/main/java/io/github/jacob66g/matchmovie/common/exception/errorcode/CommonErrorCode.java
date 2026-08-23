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
            "The requested sort property is not supported. See messageParams for allowed values."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_BAD_REQUEST",
            "The request is missing a required parameter or is otherwise malformed."),
    MALFORMED_REQUEST_BODY(HttpStatus.BAD_REQUEST, "COMMON_MALFORMED_REQUEST_BODY",
            "The request body could not be parsed."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_NOT_FOUND",
            "No resource exists at the requested path."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_METHOD_NOT_ALLOWED",
            "The HTTP method is not supported for this path. See the Allow header."),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "COMMON_NOT_ACCEPTABLE",
            "No representation matching the Accept header is available."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_UNSUPPORTED_MEDIA_TYPE",
            "The request Content-Type is not supported."),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "COMMON_PAYLOAD_TOO_LARGE",
            "The request payload exceeds the allowed size."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "COMMON_SERVICE_UNAVAILABLE",
            "The service is temporarily unable to handle the request.");

    private final HttpStatus status;
    private final String code;
    private final String detail;

    public static CommonErrorCode forStatus(HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> UNAUTHORIZED;
            case FORBIDDEN -> ACCESS_DENIED;
            case NOT_FOUND -> NOT_FOUND;
            case METHOD_NOT_ALLOWED -> METHOD_NOT_ALLOWED;
            case NOT_ACCEPTABLE -> NOT_ACCEPTABLE;
            case UNSUPPORTED_MEDIA_TYPE -> UNSUPPORTED_MEDIA_TYPE;
            case PAYLOAD_TOO_LARGE -> PAYLOAD_TOO_LARGE;
            case SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE;
            default -> status.is4xxClientError() ? BAD_REQUEST : INTERNAL_ERROR;
        };
    }

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
