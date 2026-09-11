package io.github.jacob66g.matchmovie.movies.repository;

import io.github.jacob66g.matchmovie.movies.model.Genre;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

    @Query("select m.id from Movie m where m.id in :ids")
    Set<Long> findExistingIds(@Param("ids") Set<Long> ids);

    Page<Movie> findAll(Specification<Movie> spec, Pageable pageable);

    interface Specs {
        static Specification<Movie> titleContains(String title) {
            return ((root, query, cb) ->
                    !StringUtils.hasText(title)
                            ? null
                            : cb.like(
                            cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"
                    ));
        }

        static Specification<Movie> releaseDateFrom(LocalDate from) {
            return ((root, query, cb) ->
                    from == null
                            ? null
                            : cb.greaterThanOrEqualTo(
                            root.get("releaseDate"), from
                    ));
        }

        static Specification<Movie> releaseDateTo(LocalDate to) {
            return ((root, query, cb) ->
                    to == null
                            ? null
                            : cb.lessThanOrEqualTo(
                            root.get("releaseDate"), to
                    ));
        }

        static Specification<Movie> runtimeGTE(Integer runtime) {
            return ((root, query, cb) ->
                    runtime == null
                            ? null
                            : cb.greaterThanOrEqualTo(
                            root.get("runtime"), runtime
                    ));
        }

        static Specification<Movie> runtimeLTE(Integer runtime) {
            return ((root, query, cb) ->
                    runtime == null
                            ? null
                            : cb.lessThanOrEqualTo(
                            root.get("runtime"), runtime
                    ));
        }


        static Specification<Movie> hasAllGenres(List<String> genres) {
            if (genres == null || genres.isEmpty()) {
                return null;
            }

            return (root, query, cb) -> {
                Subquery<Long> subquery = query.subquery(Long.class);

                Root<Movie> subRoot = subquery.from(Movie.class);
                Join<Movie, Genre> genreJoin = subRoot.join("genres");

                subquery
                        .select(subRoot.get("id"))
                        .where(genreJoin.get("name").in(genres))
                        .groupBy(subRoot.get("id"))
                        .having(cb.equal(
                                cb.countDistinct(genreJoin.get("name")),
                                genres.size()
                        ));

                return root.get("id").in(subquery);
            };
        }
    }
}
