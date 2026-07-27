package io.github.jacob66g.matchmovie.common.exception;

import org.slf4j.event.Level;

public class InvalidTokenException extends ApplicationException {
    public InvalidTokenException(Level logLevel, String logMessage, String messageKey, Object... args) {
        super(logLevel, logMessage, messageKey, args);
    }
}
