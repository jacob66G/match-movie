package io.github.jacob66g.matchmovie.common.exception;

import io.github.jacob66g.matchmovie.common.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.LocaleResolver;

import java.time.Instant;
import java.util.Locale;


@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        logApplicationException(Level.ERROR, "Unexpected error at %s".formatted(request.getRequestURI()), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "error.internal", null, request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(InvalidTokenException ex, HttpServletRequest request) {
        logApplicationException(ex);
        return build(HttpStatus.UNAUTHORIZED, ex, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(HttpServletRequest request) {
        String logMessage = "Unauthorized access attempt: URI=[%s] %s | UserAgent=[%s]"
                .formatted(request.getMethod(), request.getRequestURI(), request.getHeader("User-Agent"));
        logApplicationException(Level.WARN, logMessage, null);

        return build(HttpStatus.UNAUTHORIZED, "error.unauthorized", null, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(HttpServletRequest request) {
        String logMessage = "Access Denied: User [%s] tried to access protected URL [%s] with method [%s] from IP [%s]"
                .formatted(extractUsername(), request.getRequestURI(), request.getMethod(), request.getRemoteAddr());
        logApplicationException(Level.WARN, logMessage, null);

        return build(HttpStatus.FORBIDDEN, "error.access.denied", null, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, ApplicationException ex, HttpServletRequest request) {
        return build(status, ex.getMessageKey(), ex.getArgs(), request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String messageKey, Object[] args, HttpServletRequest request) {
        Locale locale = localeResolver.resolveLocale(request);
        String message = messageSource.getMessage(messageKey, args, locale);
        ErrorResponse response = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(response);
    }

    private void logApplicationException(ApplicationException ex) {
        logApplicationException(ex.getLogLevel(), ex.getLogMessage(), ex);
    }

    private void logApplicationException(Level logLevel, String logMessage, Exception ex) {
        switch (logLevel) {
            case WARN -> log.warn(logMessage);
            case ERROR -> log.error(logMessage, ex);
            default -> log.info(logMessage);
        }
    }

    private String extractUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null && !preferredUsername.isBlank()) {
                return preferredUsername;
            }
            String email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }

        return "ANONYMOUS";
    }
}
