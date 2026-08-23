package io.github.jacob66g.matchmovie.movies.repository;

import io.github.jacob66g.matchmovie.movies.model.MovieTranslation;
import io.github.jacob66g.matchmovie.movies.model.MovieTranslationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieTranslationRepository extends JpaRepository<MovieTranslation, MovieTranslationId> {
}
