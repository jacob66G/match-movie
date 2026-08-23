package io.github.jacob66g.matchmovie.common.exception;

import io.github.jacob66g.matchmovie.common.exception.errorcode.ErrorCode;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class ApplicationException extends RuntimeException {

    private final transient ErrorCode errorCode;

    private final transient Map<String, Object> messageParams = new LinkedHashMap<>();

    private final transient Map<String, Object> context = new LinkedHashMap<>();


    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.code());
        this.errorCode = errorCode;
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

    /**
     * Adds a value returned to the client so it can interpolate its own localized message.
     * Never pass anything sensitive here.
     */
    public ApplicationException param(String key, Object value) {
        messageParams.put(key, value);
        return this;
    }

    public Map<String, Object> getMessageParams() {
        return Collections.unmodifiableMap(messageParams);
    }

    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }
}
