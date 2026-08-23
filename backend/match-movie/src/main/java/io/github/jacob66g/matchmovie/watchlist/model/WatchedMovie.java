package io.github.jacob66g.matchmovie.watchlist.model;

import io.github.jacob66g.matchmovie.movies.model.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "watched_movies")
public class WatchedMovie implements Persistable<UserMovieId> {

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

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

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

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}
