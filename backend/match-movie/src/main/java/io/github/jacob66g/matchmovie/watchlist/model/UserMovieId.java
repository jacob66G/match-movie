package io.github.jacob66g.matchmovie.watchlist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserMovieId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "user_id")
    private UUID userId;

    private Long movieId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserMovieId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(movieId, that.movieId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, movieId);
    }
}
