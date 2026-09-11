package io.github.jacob66g.matchmovie.watchlist.controller;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.common.sort.SortWhitelist;
import io.github.jacob66g.matchmovie.security.annotations.AuthenticatedUser;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchedRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.UpdateWatchedMovieRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchedMovieResponse;
import io.github.jacob66g.matchmovie.watchlist.service.WatchedMovieFacade;
import io.github.jacob66g.matchmovie.watchlist.dto.sort.WatchedMovieSortField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me/watched")
@RequiredArgsConstructor
public class WatchedMovieController {

    private static final SortWhitelist<WatchedMovieSortField> SORT_WHITELIST = SortWhitelist.of(WatchedMovieSortField.class);

    private final WatchedMovieFacade watchedMovieFacade;

    @GetMapping("/{movieId}")
    public ResponseEntity<WatchedMovieResponse> getWatchedMovie(@AuthenticatedUser UUID userId,
                                                                @PathVariable @Positive Long movieId) {
        return ResponseEntity.ok(watchedMovieFacade.getWatchedMovie(userId, movieId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<WatchedMovieResponse>> getWatchedMovies(
            @AuthenticatedUser UUID userId,
            @PageableDefault(size = 20, sort = "watchedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(watchedMovieFacade.getWatchedMovies(userId, SORT_WHITELIST.sanitize(pageable)));
    }

    @PostMapping
    public ResponseEntity<WatchedMovieResponse> addToWatched(@AuthenticatedUser UUID userId,
                                                             @Valid @RequestBody AddToWatchedRequest addToWatched) {
        WatchedMovieResponse added = watchedMovieFacade.addToWatched(userId, addToWatched);
        return ResponseEntity.created(URI.create("/api/users/me/watched/" + added.movie().id())).body(added);
    }

    @PatchMapping("/{movieId}")
    public ResponseEntity<WatchedMovieResponse> updateWatchedMovie(@AuthenticatedUser UUID userId,
                                                                   @PathVariable @Positive Long movieId,
                                                                   @Valid @RequestBody UpdateWatchedMovieRequest updateWatchedMovie) {
        return ResponseEntity.ok(watchedMovieFacade.updateWatchedMovie(userId, movieId, updateWatchedMovie));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> removeFromWatched(@AuthenticatedUser UUID userId, @PathVariable @Positive Long movieId) {
        watchedMovieFacade.removeFromWatched(userId, movieId);
        return ResponseEntity.noContent().build();
    }
}
