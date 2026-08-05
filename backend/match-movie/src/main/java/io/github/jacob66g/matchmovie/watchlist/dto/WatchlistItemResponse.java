package io.github.jacob66g.matchmovie.watchlist.dto;

import io.github.jacob66g.matchmovie.movies.dto.MovieSummaryResponse;
import io.github.jacob66g.matchmovie.watchlist.model.WatchlistItem;

import java.time.Instant;

public record WatchlistItemResponse(
        MovieSummaryResponse movie,
        Instant addedAt
) {

    public static WatchlistItemResponse from(WatchlistItem watchlistItem) {
        return new WatchlistItemResponse(
                MovieSummaryResponse.from(watchlistItem.getMovie()),
                watchlistItem.getAddedAt()
        );
    }
}
