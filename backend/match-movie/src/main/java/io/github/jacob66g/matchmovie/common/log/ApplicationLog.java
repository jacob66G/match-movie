package io.github.jacob66g.matchmovie.common.log;

import org.slf4j.event.Level;

public record ApplicationLog(
        Level level,
        String message,
        Object... args
) {
}
