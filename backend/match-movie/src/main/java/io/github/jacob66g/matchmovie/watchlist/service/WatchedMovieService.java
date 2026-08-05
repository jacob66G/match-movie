package io.github.jacob66g.matchmovie.watchlist.service;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.repository.MovieRepository;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchedRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.UpdateWatchedMovieRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchedMovieResponse;
import io.github.jacob66g.matchmovie.watchlist.exception.WatchedMovieErrorCode;
import io.github.jacob66g.matchmovie.watchlist.model.UserMovieId;
import io.github.jacob66g.matchmovie.watchlist.model.WatchedMovie;
import io.github.jacob66g.matchmovie.watchlist.repository.WatchedMovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WatchedMovieService {

    private final WatchedMovieRepository watchedMovieRepository;
    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public WatchedMovieResponse getWatchedMovie(UUID userId, Long movieId) {
        return WatchedMovieResponse.from(findOrThrow(new UserMovieId(userId, movieId)));
    }

    @Transactional(readOnly = true)
    public PageResponse<WatchedMovieResponse> getWatchedMovies(UUID userId, Pageable pageable) {
        return PageResponse.from(watchedMovieRepository.findAllById_UserId(userId, pageable), WatchedMovieResponse::from);
    }

    @Transactional
    public WatchedMovieResponse addToWatched(UUID userId, AddToWatchedRequest addToWatched) {
        UserMovieId id = new UserMovieId(userId, addToWatched.movieId());

        if (watchedMovieRepository.existsById(id)) {
            throw new ApplicationException(WatchedMovieErrorCode.ALREADY_IN_WATCHED_MOVIES)
                    .with("movieId", addToWatched.movieId());
        }

        //TODO after implementing TMDB integration import the movie when it is missing from the catalogue
        Movie movie = findMovieOrThrow(addToWatched.movieId());

        WatchedMovie saved = watchedMovieRepository.save(
                new WatchedMovie(userId, movie, addToWatched.rating(), addToWatched.review())
        );

        return WatchedMovieResponse.from(saved);
    }

    @Transactional
    public WatchedMovieResponse updateWatchedMovie(UUID userId, Long movieId, UpdateWatchedMovieRequest updateWatchedMovie) {
        WatchedMovie watchedMovie = findOrThrow(new UserMovieId(userId, movieId));

        if (updateWatchedMovie.rating() != null) {
            watchedMovie.setRating(updateWatchedMovie.rating());
        }
        if (updateWatchedMovie.review() != null) {
            watchedMovie.setReview(updateWatchedMovie.review());
        }

        return WatchedMovieResponse.from(watchedMovie);
    }

    @Transactional
    public void removeFromWatched(UUID userId, Long movieId) {
        UserMovieId id = new UserMovieId(userId, movieId);

        if (!watchedMovieRepository.existsById(id)) {
            throw new ApplicationException(WatchedMovieErrorCode.WATCHED_MOVIE_NOT_FOUND)
                    .with("movieId", movieId);
        }

        watchedMovieRepository.deleteById(id);
    }

    private WatchedMovie findOrThrow(UserMovieId id) {
        return watchedMovieRepository.findWithMovieById(id)
                .orElseThrow(() -> new ApplicationException(WatchedMovieErrorCode.WATCHED_MOVIE_NOT_FOUND)
                        .with("movieId", id.getMovieId()));
    }

    private Movie findMovieOrThrow(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new ApplicationException(MovieErrorCode.MOVIE_NOT_FOUND)
                        .with("movieId", movieId));
    }
}
