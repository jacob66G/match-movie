package io.github.jacob66g.matchmovie.watchlist.service;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.repository.MovieRepository;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchlistRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchlistItemResponse;
import io.github.jacob66g.matchmovie.watchlist.exception.WatchlistErrorCode;
import io.github.jacob66g.matchmovie.watchlist.model.UserMovieId;
import io.github.jacob66g.matchmovie.watchlist.model.WatchlistItem;
import io.github.jacob66g.matchmovie.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public PageResponse<WatchlistItemResponse> getWatchlist(UUID userId, Pageable pageable) {
        return PageResponse.from(watchlistRepository.findAllById_UserId(userId, pageable), WatchlistItemResponse::from);
    }

    @Transactional
    public WatchlistItemResponse addToWatchlist(UUID userId, AddToWatchlistRequest addToWatchlist) {
        UserMovieId id = new UserMovieId(userId, addToWatchlist.movieId());

        if (watchlistRepository.existsById(id)) {
            throw new ApplicationException(WatchlistErrorCode.ALREADY_IN_WATCHLIST)
                    .with("movieId", addToWatchlist.movieId());
        }

        //TODO after implementing TMDB integration import the movie when it is missing from the catalogue
        Movie movie = findMovieOrThrow(addToWatchlist.movieId());

        WatchlistItem saved = watchlistRepository.save(new WatchlistItem(userId, movie));
        return WatchlistItemResponse.from(saved);
    }

    @Transactional
    public void removeFromWatchlist(UUID userId, Long movieId) {
        UserMovieId id = new UserMovieId(userId, movieId);

        if (!watchlistRepository.existsById(id)) {
            throw new ApplicationException(WatchlistErrorCode.WATCHLIST_ITEM_NOT_FOUND)
                    .with("movieId", movieId);
        }

        watchlistRepository.deleteById(id);
    }

    private Movie findMovieOrThrow(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new ApplicationException(MovieErrorCode.MOVIE_NOT_FOUND)
                        .with("movieId", movieId));
    }
}
