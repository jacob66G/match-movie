package io.github.jacob66g.matchmovie.common.exception;

import lombok.Getter;
import org.slf4j.event.Level;


@Getter
public abstract class ApplicationException extends RuntimeException {
    private Level logLevel;
    private String logMessage;
    private String messageKey;
    private Object[] args;

    protected ApplicationException(Level logLevel, String logMessage, String messageKey, Object... args) {
        super(messageKey);
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.messageKey = messageKey;
        this.args = args;
    }
}
