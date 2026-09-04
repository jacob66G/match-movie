package io.github.jacob66g.matchmovie.movies.seed;

import io.github.jacob66g.matchmovie.movies.client.dto.TmdbDiscoverQuery;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbSortField;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class MovieSeedPlannerTest {

    @Test
    void should_generate_queries_for_every_year_in_range() {
        //given
        MovieSeedPlanner planner = new MovieSeedPlanner(createProperties(2025, 2026));

        //when
        List<TmdbDiscoverQuery> result = planner.plan();

        //then
        assertThat(result)
                .extracting(
                        TmdbDiscoverQuery::releaseDateFrom,
                        TmdbDiscoverQuery::releaseDateTo,
                        TmdbDiscoverQuery::minVoteCount,
                        TmdbDiscoverQuery::sortBy)
                .containsExactly(
                        tuple(LocalDate.of(2026, Month.JANUARY, 1), LocalDate.of(2026, Month.DECEMBER, 31), 100, TmdbSortField.VOTE_COUNT_DESC),
                        tuple(LocalDate.of(2025, Month.JANUARY, 1), LocalDate.of(2025, Month.DECEMBER, 31), 100, TmdbSortField.VOTE_COUNT_DESC));
    }

    @Test
    void should_generate_single_query_when_years_are_the_same() {
        //given
        MovieSeedPlanner planner = new MovieSeedPlanner(createProperties(2026, 2026));

        //when
        List<TmdbDiscoverQuery> result = planner.plan();

        //then
        assertThat(result)
                .extracting(
                        TmdbDiscoverQuery::releaseDateFrom,
                        TmdbDiscoverQuery::releaseDateTo,
                        TmdbDiscoverQuery::minVoteCount,
                        TmdbDiscoverQuery::sortBy)
                .containsExactly(
                        tuple(LocalDate.of(2026, Month.JANUARY, 1), LocalDate.of(2026, Month.DECEMBER, 31), 100, TmdbSortField.VOTE_COUNT_DESC)
                );
    }

    private MovieSeedProperties createProperties(int yearFrom, int yearTo) {
        return new MovieSeedProperties(
                yearFrom,
                yearTo,
                100,
                10,
                5000,
                3,
                100,
                Duration.ZERO
        );
    }
}
