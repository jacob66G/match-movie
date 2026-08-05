package io.github.jacob66g.matchmovie.watchlist.model;

import io.github.jacob66g.matchmovie.movies.model.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "watchlist")
public class WatchlistItem {

    @EmbeddedId
    private UserMovieId id;

    @MapsId("movieId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    public WatchlistItem(UUID userId, Movie movie) {
        this.id = new UserMovieId(userId, movie.getId());
        this.movie = movie;
    }

    @PrePersist
    public void onCreate() {
        this.addedAt = Instant.now();
    }
}
