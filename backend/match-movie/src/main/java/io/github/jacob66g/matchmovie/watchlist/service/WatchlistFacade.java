package io.github.jacob66g.matchmovie.watchlist.service;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.service.MovieCatalogFacade;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchlistRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchlistItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class WatchlistFacade {

    private final MovieCatalogFacade movieCatalogFacade;
    private final WatchlistService watchlistService;

    public PageResponse<WatchlistItemResponse> getWatchlist(UUID userId, Pageable pageable) {
        return watchlistService.getWatchlist(userId, pageable);
    }

    public WatchlistItemResponse addToWatchlist(UUID userId, AddToWatchlistRequest addToWatchlist) {
        Movie movie = movieCatalogFacade.getOrImport(addToWatchlist.movieId());
        return watchlistService.addToWatchlist(userId, movie);
    }

    public void removeFromWatchlist(UUID userId, Long movieId) {
        watchlistService.removeFromWatchlist(userId, movieId);
    }
}
