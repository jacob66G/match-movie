package io.github.jacob66g.matchmovie.movies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MovieTranslationId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long movieId;

    @Column(name = "language_code", length = 10)
    private String languageCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieTranslationId that)) return false;
        return Objects.equals(movieId, that.movieId) && Objects.equals(languageCode, that.languageCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, languageCode);
    }
}
