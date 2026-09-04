package io.github.jacob66g.matchmovie.movies.seed;

import io.github.jacob66g.matchmovie.movies.client.dto.TmdbDiscoverQuery;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbSortField;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class MovieSeedPlanner {

    private final MovieSeedProperties seedProperties;

    public List<TmdbDiscoverQuery> plan() {
        List<TmdbDiscoverQuery> queries = new ArrayList<>();

        for (int year = seedProperties.yearTo(); year >= seedProperties.yearFrom(); year--) {
            queries.add(new TmdbDiscoverQuery(
                    LocalDate.of(year, Month.JANUARY, 1),
                    LocalDate.of(year, Month.DECEMBER, 31),
                    seedProperties.minVoteCount(),
                    TmdbSortField.VOTE_COUNT_DESC
            ));
        }

        return queries;
    }
}
