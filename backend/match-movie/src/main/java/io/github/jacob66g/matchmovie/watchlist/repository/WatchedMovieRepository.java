package io.github.jacob66g.matchmovie.watchlist.repository;

import io.github.jacob66g.matchmovie.watchlist.model.UserMovieId;
import io.github.jacob66g.matchmovie.watchlist.model.WatchedMovie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WatchedMovieRepository extends JpaRepository<WatchedMovie, UserMovieId> {

    @EntityGraph(attributePaths = "movie")
    Page<WatchedMovie> findAllById_UserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = "movie")
    Optional<WatchedMovie> findWithMovieById(UserMovieId id);
}
