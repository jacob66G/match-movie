package io.github.jacob66g.matchmovie.movies.client.mapper;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbMovieSearchResultResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbSearchResponse;
import io.github.jacob66g.matchmovie.movies.dto.MovieSearchResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.github.jacob66g.matchmovie.movies.MovieFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class TmdbSearchMapperTest {

    private static final int TMDB_PAGE_SIZE = 20;

    private static final List<TmdbMovieSearchResultResponse> tmdbSearchResultList =
            List.of(tmdbSearchResult(MOVIE_ID, TITLE), tmdbSearchResult(SECOND_MOVIE_ID, SECOND_TITLE));

    private final TmdbSearchMapper mapper = new TmdbSearchMapper();

    @Test
    void should_keep_the_one_based_page_reported_by_tmdb() {
        //given
        TmdbSearchResponse searchResponse = tmdbSearchResponse(3, 7, 132, tmdbSearchResultList);

        //when
        PageResponse<MovieSearchResponse> page = mapper.toSearchPage(searchResponse, Set.of());

        //then
        assertThat(page.page()).isEqualTo(3);
        assertThat(page.size()).isEqualTo(TMDB_PAGE_SIZE);
        assertThat(page.totalElements()).isEqualTo(132);
        assertThat(page.totalPages()).isEqualTo(7);
        assertThat(page.last()).isFalse();
    }

    @Test
    void should_mark_the_final_page_as_last() {
        //when
        PageResponse<MovieSearchResponse> page = mapper.toSearchPage(tmdbSearchResponse(7, 7, 132, tmdbSearchResultList), Set.of());

        //then
        assertThat(page.page()).isEqualTo(7);
        assertThat(page.last()).isTrue();
    }

    @Test
    void should_mark_an_empty_result_set_as_the_last_page() {
        //given
        TmdbSearchResponse searchResponse = new TmdbSearchResponse(1, List.of(), 0, 0);

        //when
        PageResponse<MovieSearchResponse> page = mapper.toSearchPage(searchResponse, Set.of());

        //then
        assertThat(page.content()).isEmpty();
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.totalElements()).isZero();
        assertThat(page.last()).isTrue();
    }

    @Test
    void should_flag_only_the_movies_already_in_the_catalog() {
        //when
        PageResponse<MovieSearchResponse> page = mapper.toSearchPage(
                tmdbSearchResponse(1, 1, 2, tmdbSearchResultList), Set.of(MOVIE_ID)
        );

        //then
        assertThat(page.content())
                .extracting(MovieSearchResponse::id, MovieSearchResponse::title, MovieSearchResponse::inCatalog)
                .containsExactly(
                        tuple(MOVIE_ID, TITLE, true),
                        tuple(SECOND_MOVIE_ID, SECOND_TITLE, false)
                );
    }

    @Test
    void should_drop_results_that_cannot_be_rendered() {
        //given
        TmdbSearchResponse searchResponse = new TmdbSearchResponse(
                1,
                List.of(tmdbSearchResult(null, "Missing id"), tmdbSearchResult(SECOND_MOVIE_ID, "  "), tmdbSearchResult(MOVIE_ID, "Fight Club")),
                1,
                3
        );

        //when
        PageResponse<MovieSearchResponse> page = mapper.toSearchPage(searchResponse, Set.of());

        //then
        assertThat(page.content())
                .extracting(MovieSearchResponse::id)
                .containsExactly(MOVIE_ID);
        assertThat(page.totalElements()).isEqualTo(3);
    }

    @Test
    void should_collect_the_ids_worth_checking_against_the_catalog() {
        //given
        TmdbSearchResponse searchResponse = new TmdbSearchResponse(
                1,
                List.of(tmdbSearchResult(MOVIE_ID, TITLE), tmdbSearchResult(null, "Missing id")),
                1,
                2
        );

        //when + then
        assertThat(mapper.toMovieIds(searchResponse)).containsExactly(MOVIE_ID);
    }

    @Test
    void should_return_no_ids_when_tmdb_omits_the_result_list() {
        //given
        TmdbSearchResponse searchResponse = new TmdbSearchResponse(1, null, 0, 0);

        //when + then
        assertThat(mapper.toMovieIds(searchResponse)).isEmpty();
    }

}
