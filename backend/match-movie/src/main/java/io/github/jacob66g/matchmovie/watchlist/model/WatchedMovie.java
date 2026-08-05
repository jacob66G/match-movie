package io.github.jacob66g.matchmovie.watchlist.model;

import io.github.jacob66g.matchmovie.movies.model.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "watched_movies")
public class WatchedMovie {

    @EmbeddedId
    private UserMovieId id;

    @MapsId("movieId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Setter
    @Column(name = "rating")
    private Integer rating;

    @Setter
    @Column(name = "review", length = 2000)
    private String review;

    @Column(name = "watched_at", nullable = false, updatable = false)
    private Instant watchedAt;

    public WatchedMovie(UUID userId, Movie movie, Integer rating, String review) {
        this.id = new UserMovieId(userId, movie.getId());
        this.movie = movie;
        this.rating = rating;
        this.review = review;
    }

    @PrePersist
    public void onCreate() {
        this.watchedAt = Instant.now();
    }
}
