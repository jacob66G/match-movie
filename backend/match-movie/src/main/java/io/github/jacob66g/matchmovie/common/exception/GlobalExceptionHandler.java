package io.github.jacob66g.matchmovie.common.exception;

import io.github.jacob66g.matchmovie.common.exception.dto.ErrorResponse;
import io.github.jacob66g.matchmovie.common.exception.errorcode.CommonErrorCode;
import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import io.github.jacob66g.matchmovie.common.log.LogContext;
import io.github.jacob66g.matchmovie.common.log.LogPolicy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.LocaleResolver;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.jacob66g.matchmovie.common.log.LogSanitizer.sanitize;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private static final Object[] NO_ARGS = new Object[0];

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex, HttpServletRequest request) {
        return handle(ex, ex.getErrorCode(), ex.getMessageArgs(), ex.getContext(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.VALIDATION_ERROR, NO_ARGS, Map.of("fields", formatFieldErrorsForLog(ex)), formatFieldErrors(ex), request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReference(PropertyReferenceException ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.VALIDATION_ERROR, NO_ARGS, Map.of("property", sanitize(ex.getPropertyName())), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.UNAUTHORIZED, NO_ARGS, Map.of("reason", sanitize(ex.getMessage())), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.ACCESS_DENIED, NO_ARGS, Map.of("ip", sanitize(request.getRemoteAddr())), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.INTERNAL_ERROR, NO_ARGS, Map.of("type", ex.getClass().getSimpleName()), request);
    }

    private ResponseEntity<ErrorResponse> handle(Exception ex, ErrorCode errorCode, Object[] messageArgs,
                                                 Map<String, Object> context, HttpServletRequest request) {
        return handle(ex, errorCode, messageArgs, context, null, request);
    }

    private ResponseEntity<ErrorResponse> handle(Exception ex, ErrorCode errorCode, Object[] messageArgs,
                                                 Map<String, Object> context, Map<String, String> fieldErrors,
                                                 HttpServletRequest request) {
        HttpStatus status = errorCode.status();
        logFailure(ex, errorCode, status, context, request);
        return ResponseEntity.status(status).body(buildBody(errorCode, status, messageArgs, fieldErrors, request));
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


    private ErrorResponse buildBody(ErrorCode errorCode, HttpStatus status, Object[] messageArgs,
                                    Map<String, String> fieldErrors, HttpServletRequest request) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode.code(),
                resolveMessage(errorCode, messageArgs, localeResolver.resolveLocale(request)),
                request.getRequestURI(),
                LogContext.requestId(),
                fieldErrors
        );
    }

    private String resolveMessage(ErrorCode errorCode, Object[] messageArgs, Locale locale) {
        try {
            return messageSource.getMessage(errorCode.messageKey(), messageArgs, locale);
        } catch (NoSuchMessageException ex) {
            log.warn("Missing i18n key [{}] for error code [{}] and locale [{}]", errorCode.messageKey(), errorCode.code(), locale);
            return errorCode.code();
        }
    }

    private Map<String, String> formatFieldErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> Objects.requireNonNullElse(fieldError.getDefaultMessage(), "not valid"),
                        (ms1, ms2) -> ms1 + ", " + ms2,
                        LinkedHashMap::new
                ));
    }

    private String formatFieldErrorsForLog(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + "=" + fieldError.getCode())
                .collect(Collectors.joining("; "));
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
