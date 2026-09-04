package io.github.jacob66g.matchmovie.movies.seed;

import io.github.jacob66g.matchmovie.movies.client.TmdbClient;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbDiscoverQuery;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbMovieSearchResultResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbSortField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.github.jacob66g.matchmovie.movies.MovieFactory.tmdbSearchResponse;
import static io.github.jacob66g.matchmovie.movies.MovieFactory.tmdbSearchResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieCandidateCollectorTest {

    @Mock
    private TmdbClient tmdbClient;

    @Test
    void should_collect_movie_ids_in_discover_order() {
        //given
        MovieCandidateCollector collector = createCollector(2, 500);
        List<TmdbDiscoverQuery> queries = createQueries(2);

        mockTmdbPage(queries.get(0), 1, 10, 1L, 2L);
        mockTmdbPage(queries.get(0), 2, 10, 3L, 4L);
        mockTmdbPage(queries.get(1), 1, 10, 5L);
        mockTmdbNullResultsPage(queries.get(1), 2);

        //when
        Set<Long> result = collector.collect(queries);

        //then
        verify(tmdbClient).discoverMovies(queries.get(0), 1);
        verify(tmdbClient).discoverMovies(queries.get(0), 2);
        verify(tmdbClient).discoverMovies(queries.get(1), 1);
        verify(tmdbClient).discoverMovies(queries.get(1), 2);

        assertThat(result).containsExactly(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    void should_collect_no_more_than_pages_per_slice() {
        //given
        MovieCandidateCollector collector = createCollector(1, 500);
        List<TmdbDiscoverQuery> queries = createQueries(1);

        mockTmdbPage(queries.getFirst(), 1, 10, 1L, 2L);

        //when
        Set<Long> result = collector.collect(queries);

        //then
        verify(tmdbClient).discoverMovies(queries.getFirst(), 1);
        verify(tmdbClient, never()).discoverMovies(queries.getFirst(), 2);

        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    void should_stop_after_reaching_the_candidate_limit() {
        //given
        MovieCandidateCollector collector = createCollector(2, 1);
        List<TmdbDiscoverQuery> queries = createQueries(1);

        mockTmdbPage(queries.getFirst(), 1, 10, 1L, 2L);

        //when
        Set<Long> result = collector.collect(queries);

        //then
        verify(tmdbClient).discoverMovies(queries.getFirst(), 1);
        verify(tmdbClient, never()).discoverMovies(queries.getFirst(), 2);

        assertThat(result).containsExactly(1L);
    }

    @Test
    void should_not_execute_next_queries_if_limit_reached_earlier() {
        //given
        MovieCandidateCollector collector = createCollector(2, 2);
        List<TmdbDiscoverQuery> queries = createQueries(2);

        mockTmdbPage(queries.get(0), 1, 10, 1L, 2L);

        //when
        Set<Long> result = collector.collect(queries);

        //then
        verify(tmdbClient, never()).discoverMovies(eq(queries.get(1)), anyInt());
        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    void should_stop_when_total_pages_reached_even_if_pages_per_slice_is_higher() {
        //given
        MovieCandidateCollector collector = createCollector(10, 500);
        List<TmdbDiscoverQuery> queries = createQueries(1);

        mockTmdbPage(queries.getFirst(), 1, 1, 1L);

        //when
        Set<Long> result = collector.collect(queries);

        //then
        verify(tmdbClient).discoverMovies(queries.getFirst(), 1);
        verify(tmdbClient, never()).discoverMovies(queries.getFirst(), 2);
        assertThat(result).containsExactly(1L);
    }

    @Test
    void should_keep_the_first_occurrence_when_the_same_id_appears_in_two_queries() {
        //given
        MovieCandidateCollector collector = createCollector(1, 500);
        List<TmdbDiscoverQuery> queries = createQueries(2);

        mockTmdbPage(queries.get(0), 1, 1, 1L, 2L);
        mockTmdbPage(queries.get(1), 1, 1, 2L, 3L);

        //when
        Set<Long> result = collector.collect(queries);

        //then
        assertThat(result).containsExactly(1L, 2L, 3L);
    }

    @Test
    void should_collect_every_id_when_there_is_no_candidate_limit() {
        //given
        MovieCandidateCollector collector = createCollector(2, null);
        List<TmdbDiscoverQuery> queries = createQueries(1);

        mockTmdbPage(queries.getFirst(), 1, 2, 1L, 2L);
        mockTmdbPage(queries.getFirst(), 2, 2, 3L, 4L);

        //when
        Set<Long> result = collector.collect(queries);

        //then
        verify(tmdbClient).discoverMovies(queries.getFirst(), 1);
        verify(tmdbClient).discoverMovies(queries.getFirst(), 2);
        assertThat(result).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void should_stop_query_when_a_page_has_no_results() {
        //given
        MovieCandidateCollector collector = createCollector(10, 500);
        List<TmdbDiscoverQuery> queries = createQueries(1);

        when(tmdbClient.discoverMovies(queries.getFirst(), 1))
                .thenReturn(tmdbSearchResponse(1, 10, 0, List.of()));

        //when
        Set<Long> result = collector.collect(queries);

        //then
        verify(tmdbClient).discoverMovies(queries.getFirst(), 1);
        verify(tmdbClient, never()).discoverMovies(queries.getFirst(), 2);
        assertThat(result).isEmpty();
    }

    @Test
    void should_ignore_results_without_an_id() {
        //given
        MovieCandidateCollector collector = createCollector(1, 500);
        List<TmdbDiscoverQuery> queries = createQueries(1);

        mockTmdbPage(queries.getFirst(), 1, 1, null, 10L);

        //when
        Set<Long> result = collector.collect(queries);

        //then
        assertThat(result).containsExactly(10L);
    }

    private MovieCandidateCollector createCollector(int pagesPerSlice, Integer maxCandidates) {
        MovieSeedProperties props = new MovieSeedProperties(2025, 2026, 100, pagesPerSlice, maxCandidates, 3, 100, Duration.ZERO);
        return new MovieCandidateCollector(tmdbClient, props);
    }

    private void mockTmdbPage(TmdbDiscoverQuery query, int page, int totalPages, Long... movieIds) {
        List<TmdbMovieSearchResultResponse> results = Arrays.stream(movieIds)
                .map(id -> tmdbSearchResult(id, "title"))
                .toList();

        when(tmdbClient.discoverMovies(query, page))
                .thenReturn(tmdbSearchResponse(page, totalPages, results.size(), results));
    }

    private void mockTmdbNullResultsPage(TmdbDiscoverQuery query, int page) {
        when(tmdbClient.discoverMovies(query, page))
                .thenReturn(tmdbSearchResponse(page, 1, 0, null));
    }

    private List<TmdbDiscoverQuery> createQueries(int queriesCount) {
        List<TmdbDiscoverQuery> queries = new ArrayList<>();
        int year = 2026;
        for (int i = 0; i < queriesCount; i++) {
            queries.add(new TmdbDiscoverQuery(LocalDate.of(year - i, Month.JANUARY, 1),
                    LocalDate.of(year - i, Month.DECEMBER, 31),
                    100,
                    TmdbSortField.VOTE_COUNT_DESC));
        }
        return queries;
    }
}
