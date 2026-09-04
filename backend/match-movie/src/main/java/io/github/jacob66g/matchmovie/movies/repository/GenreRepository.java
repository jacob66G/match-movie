package io.github.jacob66g.matchmovie.movies.repository;

import io.github.jacob66g.matchmovie.movies.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Modifying
    @Query(value = "INSERT INTO genres (id, name) VALUES (:id, :name) ON CONFLICT (id) DO NOTHING",
            nativeQuery = true)
    void insertIfAbsent(@Param("id") Long id, @Param("name") String name);

}
