package io.github.jacob66g.matchmovie.watchlist.controller;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.common.sort.SortWhitelist;
import io.github.jacob66g.matchmovie.security.annotations.AuthenticatedUser;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchlistRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchlistItemResponse;
import io.github.jacob66g.matchmovie.watchlist.service.WatchlistFacade;
import io.github.jacob66g.matchmovie.watchlist.sort.WatchlistSortField;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/me/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private static final SortWhitelist<WatchlistSortField> SORT_WHITELIST = SortWhitelist.of(WatchlistSortField.class);

    private final WatchlistFacade watchlistFacade;

    @GetMapping
    public ResponseEntity<PageResponse<WatchlistItemResponse>> getWatchlist(
            @AuthenticatedUser UUID userId,
            @PageableDefault(size = 20, sort = "addedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(watchlistFacade.getWatchlist(userId, SORT_WHITELIST.sanitize(pageable)));
    }

    @PostMapping
    public ResponseEntity<WatchlistItemResponse> addToWatchlist(@AuthenticatedUser UUID userId,
                                                                @Valid @RequestBody AddToWatchlistRequest addToWatchlist) {
        WatchlistItemResponse created = watchlistFacade.addToWatchlist(userId, addToWatchlist);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> removeFromWatchlist(@AuthenticatedUser UUID userId, @PathVariable Long movieId) {
        watchlistFacade.removeFromWatchlist(userId, movieId);
        return ResponseEntity.noContent().build();
    }
}
