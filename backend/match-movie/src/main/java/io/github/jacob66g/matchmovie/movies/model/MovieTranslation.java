package io.github.jacob66g.matchmovie.movies.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "movie_translations")
public class MovieTranslation implements Persistable<MovieTranslationId> {

    @EmbeddedId
    private MovieTranslationId id;

    @MapsId("movieId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Setter
    @Column(name = "title")
    private String title;

    @Setter
    @Column(name = "overview")
    private String overview;

    @Setter
    @Column(name = "tagline")
    private String tagline;

    public MovieTranslation(Movie movie, String languageCode, String title, String overview, String tagline) {
        this.id = new MovieTranslationId(movie.getId(), languageCode);
        this.movie = movie;
        this.title = title;
        this.overview = overview;
        this.tagline = tagline;
    }

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}
