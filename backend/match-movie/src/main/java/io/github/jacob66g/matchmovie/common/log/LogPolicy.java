package io.github.jacob66g.matchmovie.common.log;

import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Set;


public final class LogPolicy {

    private static final Set<Integer> SECURITY_SENSITIVE = Set.of(
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.FORBIDDEN.value()
    );

    private LogPolicy() {
    }

    public static Level levelFor(HttpStatusCode status) {
        if (status.is5xxServerError()) {
            return Level.ERROR;
        }
        if (SECURITY_SENSITIVE.contains(status.value())) {
            return Level.WARN;
        }
        return Level.DEBUG;
    }

    public static boolean includeStackTrace(HttpStatusCode status) {
        return status.is5xxServerError();
    }
}
