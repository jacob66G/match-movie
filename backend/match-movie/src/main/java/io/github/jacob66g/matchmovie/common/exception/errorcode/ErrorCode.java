package io.github.jacob66g.matchmovie.common.exception.errorcode;

import io.github.jacob66g.matchmovie.common.log.LogPolicy;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;


public interface ErrorCode {

    HttpStatus status();

    String code();

    String detail();

    default Level logLevel() {
        return LogPolicy.levelFor(status());
    }
}
