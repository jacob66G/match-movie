package io.github.jacob66g.matchmovie.watchlist.dto;

import io.github.jacob66g.matchmovie.movies.dto.MovieSummaryResponse;
import io.github.jacob66g.matchmovie.watchlist.model.WatchedMovie;

import java.time.Instant;

public record WatchedMovieResponse(
        MovieSummaryResponse movie,
        Integer rating,
        String review,
        Instant watchedAt
) {

    public static WatchedMovieResponse from(WatchedMovie watchedMovie) {
        return new WatchedMovieResponse(
                MovieSummaryResponse.from(watchedMovie.getMovie()),
                watchedMovie.getRating(),
                watchedMovie.getReview(),
                watchedMovie.getWatchedAt()
        );
    }
}
