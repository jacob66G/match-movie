package io.github.jacob66g.matchmovie.watchlist.service;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.service.MovieCatalogFacade;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchedRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.UpdateWatchedMovieRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchedMovieResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class WatchedMovieFacade {

    private final MovieCatalogFacade movieCatalogFacade;
    private final WatchedMovieService watchedMovieService;

    public WatchedMovieResponse getWatchedMovie(UUID userId, Long movieId) {
        return watchedMovieService.getWatchedMovie(userId, movieId);
    }

    public PageResponse<WatchedMovieResponse> getWatchedMovies(UUID userId, Pageable pageable) {
        return watchedMovieService.getWatchedMovies(userId, pageable);
    }

    public WatchedMovieResponse addToWatched(UUID userId, AddToWatchedRequest addToWatched) {
        Movie movie = movieCatalogFacade.getOrImport(addToWatched.movieId());
        return watchedMovieService.addToWatched(userId, movie, addToWatched);
    }

    public WatchedMovieResponse updateWatchedMovie(UUID userId, Long movieId, UpdateWatchedMovieRequest updateWatchedMovie) {
        return watchedMovieService.updateWatchedMovie(userId, movieId, updateWatchedMovie);
    }

    public void removeFromWatched(UUID userId, Long movieId) {
        watchedMovieService.removeFromWatched(userId, movieId);
    }
}
