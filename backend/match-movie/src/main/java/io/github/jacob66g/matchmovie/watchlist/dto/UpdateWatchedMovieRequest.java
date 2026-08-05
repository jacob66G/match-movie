package io.github.jacob66g.matchmovie.watchlist.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateWatchedMovieRequest(
        @Min(1)
        @Max(10)
        Integer rating,
        @Size(max = 2000)
        String review
) {
}
