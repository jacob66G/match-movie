package io.github.jacob66g.matchmovie.watchlist.dto;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.AssertTrue;
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
    @AssertTrue(message = "at least one field must be changed")
    public boolean isAtLeastOneFieldChanged() {
        return rating != null || StringUtils.isNotBlank(review);
    }
}
