package io.github.jacob66g.matchmovie.movies.repository;

import io.github.jacob66g.matchmovie.movies.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("select m.id from Movie m where m.id in :ids")
    Set<Long> findExistingIds(@Param("ids") Set<Long> ids);
}
