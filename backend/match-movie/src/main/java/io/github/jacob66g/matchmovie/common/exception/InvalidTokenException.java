package io.github.jacob66g.matchmovie.common.exception;

import io.github.jacob66g.matchmovie.common.log.ApplicationLog;

public class InvalidTokenException extends ApplicationException {
    public InvalidTokenException(ApplicationLog log, String messageKey, Object... args) {
        super(log, messageKey, args);
    }
}
