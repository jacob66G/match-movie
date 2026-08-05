package io.github.jacob66g.matchmovie.movies.repository;

import io.github.jacob66g.matchmovie.movies.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
