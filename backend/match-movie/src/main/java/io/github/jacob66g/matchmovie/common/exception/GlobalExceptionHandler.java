package io.github.jacob66g.matchmovie.common.exception;

import io.github.jacob66g.matchmovie.common.exception.dto.ErrorResponse;
import io.github.jacob66g.matchmovie.common.exception.errorcode.CommonErrorCode;
import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import io.github.jacob66g.matchmovie.common.log.LogContext;
import io.github.jacob66g.matchmovie.common.log.LogPolicy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.Errors;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.*;
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

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        Map<String, List<String>> fieldErrors = formatMethodValidationErrors(ex);

        return handle(ex, CommonErrorCode.VALIDATION_ERROR, NO_PARAMS,
                Map.of("fields", fieldErrors), fieldErrors, ex.getHeaders(), request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReference(PropertyReferenceException ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.INVALID_SORT_PROPERTY, Map.of("property", ex.getPropertyName()),
                Map.of("property", sanitize(ex.getPropertyName())), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.MALFORMED_REQUEST_BODY, NO_PARAMS,
                Map.of("reason", sanitize(ex.getMostSpecificCause().getMessage())), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return handle(ex, CommonErrorCode.BAD_REQUEST, Map.of("parameter", ex.getName()),
                Map.of("parameter", ex.getName(), "requiredType", String.valueOf(ex.getRequiredType())), request);
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
        Map<String, Object> context = Map.of("type", ex.getClass().getSimpleName());

        if (ex instanceof org.springframework.web.ErrorResponse mvcError) {
            HttpStatus status = HttpStatus.valueOf(mvcError.getStatusCode().value());
            return handle(ex, CommonErrorCode.forStatus(status), NO_PARAMS, context, null,
                    mvcError.getHeaders(), request);
        }

        return handle(ex, CommonErrorCode.INTERNAL_ERROR, NO_PARAMS, context, request);
    }

    private ResponseEntity<ErrorResponse> handle(Exception ex, ErrorCode errorCode, Map<String, Object> messageParams,
                                                 Map<String, Object> context, HttpServletRequest request) {
        return handle(ex, errorCode, messageParams, context, null, HttpHeaders.EMPTY, request);
    }

    private ResponseEntity<ErrorResponse> handle(Exception ex, ErrorCode errorCode, Map<String, Object> messageParams,
                                                 Map<String, Object> context, Map<String, List<String>> fieldErrors,
                                                 HttpServletRequest request) {
        return handle(ex, errorCode, messageParams, context, fieldErrors, HttpHeaders.EMPTY, request);
    }

    private ResponseEntity<ErrorResponse> handle(Exception ex, ErrorCode errorCode, Map<String, Object> messageParams,
                                                 Map<String, Object> context, Map<String, List<String>> fieldErrors,
                                                 HttpHeaders headers, HttpServletRequest request) {
        HttpStatus status = errorCode.status();
        logFailure(ex, errorCode, status, context, request);
        return ResponseEntity.status(status)
                .headers(headers)
                .body(buildBody(errorCode, status, messageParams, fieldErrors, request));
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
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
        addBindingErrors(fieldErrors, ex.getBindingResult());
        return fieldErrors;
    }

    private Map<String, List<String>> formatMethodValidationErrors(HandlerMethodValidationException ex) {
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                addBindingErrors(fieldErrors, parameterErrors);
            } else {
                String parameterName = Objects.requireNonNullElse(result.getMethodParameter().getParameterName(), "parameter");

                result.getResolvableErrors().forEach(error ->
                        addValidationError(fieldErrors, parameterName, messageOf(error))
                );
            }
        }

        ex.getCrossParameterValidationResults().forEach(error ->
                addValidationError(fieldErrors, "_global", messageOf(error))
        );

        return fieldErrors;
    }

    private void addBindingErrors(Map<String, List<String>> fieldErrors, Errors errors) {
        errors.getFieldErrors().forEach(error ->
                addValidationError(fieldErrors, error.getField(), messageOf(error))
        );

        errors.getGlobalErrors().forEach(error ->
                addValidationError(fieldErrors, errors.getObjectName(), messageOf(error))
        );
    }

    private void addValidationError(Map<String, List<String>> fieldErrors, String field, String message) {
        fieldErrors
                .computeIfAbsent(field, ignored -> new ArrayList<>())
                .add(message);
    }

    private String messageOf(MessageSourceResolvable error) {
        return Objects.requireNonNullElse(
                error.getDefaultMessage(),
                "Invalid value"
        );
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
