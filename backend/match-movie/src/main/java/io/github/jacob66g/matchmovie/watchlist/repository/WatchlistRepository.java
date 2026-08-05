package io.github.jacob66g.matchmovie.watchlist.repository;

import io.github.jacob66g.matchmovie.watchlist.model.UserMovieId;
import io.github.jacob66g.matchmovie.watchlist.model.WatchlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WatchlistRepository extends JpaRepository<WatchlistItem, UserMovieId> {

    @EntityGraph(attributePaths = "movie")
    Page<WatchlistItem> findAllById_UserId(UUID userId, Pageable pageable);
}
