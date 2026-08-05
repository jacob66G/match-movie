package io.github.jacob66g.matchmovie.watchlist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddToWatchlistRequest(
        @NotNull
        @Positive
        Long movieId
) {
}
