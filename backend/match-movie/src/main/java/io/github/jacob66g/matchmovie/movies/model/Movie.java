package io.github.jacob66g.matchmovie.movies.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movies")
public class Movie implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "imdb_id", length = 15)
    private String imdbId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "original_title")
    private String originalTitle;

    @Column(name = "original_language", length = 10)
    private String originalLanguage;

    @Column(name = "overview")
    private String overview;

    @Column(name = "tagline")
    private String tagline;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    /**
     * Derived by the database from {@code release_date}. Not re-read after a write, so it stays
     * stale in memory until the entity is reloaded.
     */
    @Setter(AccessLevel.NONE)
    @Column(name = "release_year", insertable = false, updatable = false)
    private Short releaseYear;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "adult", nullable = false)
    private boolean adult;

    @Column(name = "video", nullable = false)
    private boolean video;

    @Column(name = "popularity")
    private Double popularity;

    @Column(name = "vote_average", precision = 5, scale = 3)
    private BigDecimal voteAverage;

    @Column(name = "vote_count", nullable = false)
    private int voteCount;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(name = "collection_id")
    private Long collectionId;

    @Column(name = "collection_name")
    private String collectionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", nullable = false, length = 20)
    private MovieOrigin origin;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new LinkedHashSet<>();

    public void markSynced() {
        this.syncedAt = Instant.now();
    }

    @Builder.Default
    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    void onPersist() {
        if (syncedAt == null) {
            syncedAt = Instant.now();
        }
        if (genres == null) {
            genres = new LinkedHashSet<>();
        }
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

}
