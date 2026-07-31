package io.github.jacob66g.matchmovie.common.log;

import org.slf4j.MDC;


public final class LogContext {

    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";

    private LogContext() {
    }

    public static String requestId() {
        return MDC.get(REQUEST_ID);
    }

    public static void putUserId(String userId) {
        MDC.put(USER_ID, userId);
    }
}
