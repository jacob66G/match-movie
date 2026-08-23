package io.github.jacob66g.matchmovie.movies.repository;

import io.github.jacob66g.matchmovie.movies.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Modifying
    @Query(value = "INSERT INTO genres (id, name) VALUES (:id, :name) ON CONFLICT (id) DO NOTHING",
            nativeQuery = true)
    void insertIfAbsent(@Param("id") Long id, @Param("name") String name);

    /**
     * Transactional per row so the dictionary sync can fetch from TMDB outside any transaction.
     * Rows carry no cross-row invariant, so partial progress is acceptable.
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO genres (id, name) VALUES (:id, :name) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name",
            nativeQuery = true)
    void upsert(@Param("id") Long id, @Param("name") String name);
}
