package io.github.jacob66g.matchmovie.common.exception;

import io.github.jacob66g.matchmovie.common.exception.dto.ErrorResponse;
import io.github.jacob66g.matchmovie.common.exception.errorcode.CommonErrorCode;
import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import io.github.jacob66g.matchmovie.common.log.LogContext;
import io.github.jacob66g.matchmovie.common.log.LogPolicy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.jacob66g.matchmovie.common.log.LogSanitizer.sanitize;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Map<String, Object> NO_PARAMS = Map.of();

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex, HttpServletRequest request) {
        return handle(ex, ex.getErrorCode(), ex.getMessageParams(), ex.getContext(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, List<String>> fieldErrors = formatFieldErrors(ex);
        return handle(ex, CommonErrorCode.VALIDATION_ERROR, NO_PARAMS,
                Map.of("fields", fieldErrors), fieldErrors, request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReference(PropertyReferenceException ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.INVALID_SORT_PROPERTY, Map.of("property", ex.getPropertyName()),
                Map.of("property", sanitize(ex.getPropertyName())), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.UNAUTHORIZED, NO_PARAMS, Map.of("reason", sanitize(ex.getMessage())), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.ACCESS_DENIED, NO_PARAMS, Map.of("ip", sanitize(request.getRemoteAddr())), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.INTERNAL_ERROR, NO_PARAMS, Map.of("type", ex.getClass().getSimpleName()), request);
    }

    private ResponseEntity<ErrorResponse> handle(Exception ex, ErrorCode errorCode, Map<String, Object> messageParams,
                                                 Map<String, Object> context, HttpServletRequest request) {
        return handle(ex, errorCode, messageParams, context, null, request);
    }

    private ResponseEntity<ErrorResponse> handle(Exception ex, ErrorCode errorCode, Map<String, Object> messageParams,
                                                 Map<String, Object> context, Map<String, List<String>> fieldErrors,
                                                 HttpServletRequest request) {
        HttpStatus status = errorCode.status();
        logFailure(ex, errorCode, status, context, request);
        return ResponseEntity.status(status).body(buildBody(errorCode, status, messageParams, fieldErrors, request));
    }

    private void logFailure(Exception ex, ErrorCode errorCode, HttpStatus status, Map<String, Object> context, HttpServletRequest request) {
        log.atLevel(errorCode.logLevel())
                .setCause(LogPolicy.includeStackTrace(status) ? ex : null)
                .log("{} {} -> {} {} {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        status.value(),
                        errorCode.code(),
                        formatContext(context));
    }


    private ErrorResponse buildBody(ErrorCode errorCode, HttpStatus status, Map<String, Object> messageParams,
                                    Map<String, List<String>> fieldErrors, HttpServletRequest request) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode.code(),
                errorCode.detail(),
                request.getRequestURI(),
                LogContext.requestId(),
                messageParams,
                fieldErrors
        );
    }

    private Map<String, List<String>> formatFieldErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                fieldError -> Objects.requireNonNullElse(fieldError.getCode(), "Invalid"),
                                Collectors.toList())
                ));
    }

    private String formatContext(Map<String, Object> context) {
        if (context.isEmpty()) {
            return "";
        }
        return context.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + sanitize(entry.getValue()))
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
