package io.github.jacob66g.matchmovie.movies.seed;

import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.client.TmdbClient;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbDiscoverQuery;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbMovieStatus;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbSortField;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import io.github.jacob66g.matchmovie.movies.service.MovieCatalogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static io.github.jacob66g.matchmovie.movies.MovieFactory.OVERVIEW;
import static io.github.jacob66g.matchmovie.movies.MovieFactory.STATUS;
import static io.github.jacob66g.matchmovie.movies.MovieFactory.tmdbMovieDetailsResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieSeedServiceTest {

    @Mock
    private MovieCatalogService movieCatalogService;

    @Mock
    private TmdbClient tmdbClient;

    @Mock
    private MovieSeedPlanner planner;

    @Mock
    private MovieCandidateCollector collector;

    @AfterEach
    void clearInterruptFlag() {
        if (Thread.currentThread().isInterrupted()) {
            assertThat(Thread.interrupted()).isTrue();
        }
    }

    @Test
    void should_successfully_persist_all_candidates() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        Set<Long> candidateIds = candidateIds(1L, 2L, 3L);
        stubDiscovery(candidateIds, Set.of());
        candidateIds.forEach(movieId ->
                when(tmdbClient.getMovieDetails(movieId, false)).thenReturn(tmdbMovieDetailsResponse(movieId))
        );

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(tmdbClient).getMovieDetails(1L, false);
        verify(tmdbClient).getMovieDetails(2L, false);
        verify(tmdbClient).getMovieDetails(3L, false);

        verify(movieCatalogService).persistImported(tmdbMovieDetailsResponse(1L), MovieOrigin.SEED);
        verify(movieCatalogService).persistImported(tmdbMovieDetailsResponse(2L), MovieOrigin.SEED);
        verify(movieCatalogService).persistImported(tmdbMovieDetailsResponse(3L), MovieOrigin.SEED);

        assertThat(result.candidates()).isEqualTo(3);
        assertThat(result.toImport()).isEqualTo(3);
        assertThat(result.imported()).isEqualTo(3);
        assertThat(result.skipped()).isZero();
        assertThat(result.failedIds()).isEmpty();
    }

    @Test
    void should_persist_only_candidates_missing_from_the_catalog() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        Set<Long> candidateIds = candidateIds(1L, 2L, 3L);
        stubDiscovery(candidateIds, Set.of(2L, 3L));
        when(tmdbClient.getMovieDetails(1L, false)).thenReturn(tmdbMovieDetailsResponse(1L));

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(tmdbClient).getMovieDetails(1L, false);
        verify(tmdbClient, never()).getMovieDetails(2L, false);
        verify(tmdbClient, never()).getMovieDetails(3L, false);
        verify(movieCatalogService).persistImported(tmdbMovieDetailsResponse(1L), MovieOrigin.SEED);

        assertThat(result.candidates()).isEqualTo(3);
        assertThat(result.toImport()).isEqualTo(1);
        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        assertThat(result.failedIds()).isEmpty();
    }

    @Test
    void should_retry_until_max_attempts_when_tmdb_is_unavailable() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        stubDiscovery(candidateIds(1L), Set.of());
        when(tmdbClient.getMovieDetails(1L, false))
                .thenThrow(new ApplicationException(MovieErrorCode.TMDB_UNAVAILABLE));

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(tmdbClient, times(3)).getMovieDetails(1L, false);
        verify(movieCatalogService, never()).persistImported(any(), any());

        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isZero();
        assertThat(result.failedIds()).containsExactly(1L);
    }

    @Test
    void should_persist_when_a_later_attempt_succeeds() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        stubDiscovery(candidateIds(1L), Set.of());
        when(tmdbClient.getMovieDetails(1L, false))
                .thenThrow(new ApplicationException(MovieErrorCode.TMDB_UNAVAILABLE))
                .thenReturn(tmdbMovieDetailsResponse(1L));

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(tmdbClient, times(2)).getMovieDetails(1L, false);
        verify(movieCatalogService).persistImported(tmdbMovieDetailsResponse(1L), MovieOrigin.SEED);

        assertThat(result.candidates()).isEqualTo(1);
        assertThat(result.toImport()).isEqualTo(1);
        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        assertThat(result.failedIds()).isEmpty();
    }

    @Test
    void should_skip_when_movie_has_no_overview() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        stubDiscovery(candidateIds(1L), Set.of());
        when(tmdbClient.getMovieDetails(1L, false)).thenReturn(tmdbMovieDetailsResponse(1L, "  ", STATUS));

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(tmdbClient).getMovieDetails(1L, false);
        verify(movieCatalogService, never()).persistImported(any(), any());

        assertThat(result.toImport()).isEqualTo(1);
        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failedIds()).isEmpty();
    }

    @Test
    void should_skip_when_movie_is_not_released() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        stubDiscovery(candidateIds(1L), Set.of());
        when(tmdbClient.getMovieDetails(1L, false))
                .thenReturn(tmdbMovieDetailsResponse(1L, OVERVIEW, TmdbMovieStatus.IN_PRODUCTION.getApiValue()));

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(tmdbClient).getMovieDetails(1L, false);
        verify(movieCatalogService, never()).persistImported(any(), any());

        assertThat(result.toImport()).isEqualTo(1);
        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failedIds()).isEmpty();
    }

    @Test
    void should_skip_when_movie_is_missing_in_tmdb() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        stubDiscovery(candidateIds(1L), Set.of());
        when(tmdbClient.getMovieDetails(1L, false))
                .thenThrow(new ApplicationException(MovieErrorCode.TMDB_MOVIE_NOT_FOUND));

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(tmdbClient).getMovieDetails(1L, false);
        verify(movieCatalogService, never()).persistImported(any(), any());

        assertThat(result.toImport()).isEqualTo(1);
        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failedIds()).isEmpty();
    }

    @Test
    void should_skip_when_movie_was_inserted_concurrently() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        stubDiscovery(candidateIds(1L), Set.of());
        when(tmdbClient.getMovieDetails(1L, false)).thenReturn(tmdbMovieDetailsResponse(1L));
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(movieCatalogService).persistImported(any(), eq(MovieOrigin.SEED));

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(tmdbClient).getMovieDetails(1L, false);
        verify(movieCatalogService).persistImported(any(), eq(MovieOrigin.SEED));

        assertThat(result.toImport()).isEqualTo(1);
        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failedIds()).isEmpty();
    }

    @Test
    void should_count_imported_skipped_and_failed_movies_independently() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        stubDiscovery(candidateIds(1L, 2L, 3L), Set.of());
        when(tmdbClient.getMovieDetails(1L, false)).thenReturn(tmdbMovieDetailsResponse(1L));
        when(tmdbClient.getMovieDetails(2L, false)).thenReturn(tmdbMovieDetailsResponse(2L, "", STATUS));
        when(tmdbClient.getMovieDetails(3L, false))
                .thenThrow(new ApplicationException(MovieErrorCode.TMDB_UNAVAILABLE));

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(movieCatalogService).persistImported(tmdbMovieDetailsResponse(1L), MovieOrigin.SEED);

        assertThat(result.candidates()).isEqualTo(3);
        assertThat(result.toImport()).isEqualTo(3);
        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failedIds()).containsExactly(3L);
    }

    @Test
    void should_lookup_catalog_in_batches() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        Set<Long> candidateIds = LongStream.rangeClosed(1, 2005)
                .boxed()
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<TmdbDiscoverQuery> queries = createQueries(1);
        when(planner.plan()).thenReturn(queries);
        when(collector.collect(queries)).thenReturn(candidateIds);

        List<Integer> batchSizes = new ArrayList<>();
        when(movieCatalogService.findCatalogIds(anySet())).thenAnswer(invocation -> {
            batchSizes.add(invocation.<Set<Long>>getArgument(0).size());
            if (batchSizes.size() == 3) {
                Thread.currentThread().interrupt();
            }
            return Set.of();
        });

        //when
        movieSeedService.seed();

        //then
        assertThat(batchSizes).containsExactly(1000, 1000, 5);
    }

    @Test
    void should_not_call_tmdb_when_there_are_no_candidates() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        stubDiscovery(Set.of(), Set.of());

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(movieCatalogService, never()).findCatalogIds(any());
        verify(tmdbClient, never()).getMovieDetails(anyLong(), anyBoolean());
        verify(movieCatalogService, never()).persistImported(any(), any());

        assertThat(result.candidates()).isZero();
        assertThat(result.toImport()).isZero();
        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isZero();
        assertThat(result.failedIds()).isEmpty();
    }

    @Test
    void should_stop_before_importing_when_the_thread_is_already_interrupted() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        stubDiscovery(candidateIds(1L, 2L, 3L), Set.of());
        Thread.currentThread().interrupt();

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(tmdbClient, never()).getMovieDetails(anyLong(), anyBoolean());

        assertThat(result.candidates()).isEqualTo(3);
        assertThat(result.toImport()).isEqualTo(3);
        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isZero();
        assertThat(result.failedIds()).isEmpty();
    }

    @Test
    void should_stop_after_the_current_movie_when_interrupted_during_import() {
        //given
        MovieSeedService movieSeedService = createSeedService();
        stubDiscovery(candidateIds(1L, 2L, 3L), Set.of());
        when(tmdbClient.getMovieDetails(1L, false)).thenAnswer(invocation -> {
            Thread.currentThread().interrupt();
            return tmdbMovieDetailsResponse(1L);
        });

        //when
        MovieSeedReport result = movieSeedService.seed();

        //then
        verify(tmdbClient).getMovieDetails(1L, false);
        verify(tmdbClient, never()).getMovieDetails(2L, false);
        verify(tmdbClient, never()).getMovieDetails(3L, false);
        verify(movieCatalogService).persistImported(tmdbMovieDetailsResponse(1L), MovieOrigin.SEED);

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        assertThat(result.failedIds()).isEmpty();
    }

    private void stubDiscovery(Set<Long> candidateIds, Set<Long> alreadyInCatalog) {
        List<TmdbDiscoverQuery> queries = createQueries(1);
        when(planner.plan()).thenReturn(queries);
        when(collector.collect(queries)).thenReturn(candidateIds);
        if (!candidateIds.isEmpty()) {
            when(movieCatalogService.findCatalogIds(anySet())).thenReturn(alreadyInCatalog);
        }
    }

    private MovieSeedService createSeedService() {
        MovieSeedProperties props = new MovieSeedProperties(2025, 2026, 100, 10, 10000, 3, 100, Duration.ZERO);
        return new MovieSeedService(props, planner, collector, movieCatalogService, tmdbClient);
    }

    private static Set<Long> candidateIds(Long... ids) {
        return new LinkedHashSet<>(List.of(ids));
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
