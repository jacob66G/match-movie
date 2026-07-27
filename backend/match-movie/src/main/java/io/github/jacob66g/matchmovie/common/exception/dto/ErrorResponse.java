package io.github.jacob66g.matchmovie.common.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        String timestamp,
        Integer status,
        String error,
        String message,
        String path
) {
}
