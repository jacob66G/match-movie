package io.github.jacob66g.matchmovie.common.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String detail,
        String path,
        String requestId,
        Map<String, Object> messageParams,
        Map<String, List<String>> fieldErrors
) {
}
