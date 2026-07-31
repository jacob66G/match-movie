package io.github.jacob66g.matchmovie.common.exception;

import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class ApplicationException extends RuntimeException {

    private final transient ErrorCode errorCode;

    private final transient Object[] messageArgs;

    private final transient Map<String, Object> context = new LinkedHashMap<>();


    public ApplicationException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.code());
        this.errorCode = errorCode;
        this.messageArgs = messageArgs != null ? messageArgs : new Object[0];
    }

    public ApplicationException causedBy(Throwable cause) {
        initCause(cause);
        return this;
    }

    /**
     * Adds diagnostic detail that is logged but never returned to the client.
     */
    public ApplicationException with(String key, Object value) {
        context.put(key, value);
        return this;
    }

}
