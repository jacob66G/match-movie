package io.github.jacob66g.matchmovie.common.exception;

import io.github.jacob66g.matchmovie.common.log.ApplicationLog;
import lombok.Getter;


@Getter
public abstract class ApplicationException extends RuntimeException {
    private String messageKey;
    private Object[] args;
    private ApplicationLog log;

    protected ApplicationException(ApplicationLog log, String messageKey, Object... args) {
        super(messageKey);
        this.log = log;
        this.messageKey = messageKey;
        this.args = args;
    }
}
