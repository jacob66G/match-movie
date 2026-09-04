package io.github.jacob66g.matchmovie.movies.seed;

import io.github.jacob66g.matchmovie.movies.client.TmdbClient;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbDiscoverQuery;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbMovieSearchResultResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbSearchResponse;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class MovieCandidateCollector {

    private final TmdbClient tmdbClient;
    private final MovieSeedProperties seedProperties;

    public Set<Long> collect(List<TmdbDiscoverQuery> queries) {
        Set<Long> candidateIds = new LinkedHashSet<>();

        for (TmdbDiscoverQuery query : queries) {
            if (reachedLimit(candidateIds)) {
                break;
            }
            collectForQuery(query, candidateIds);
        }

        return candidateIds;
    }

    private void collectForQuery(TmdbDiscoverQuery query, Set<Long> candidateIds) {
        for (int page = 1; page <= seedProperties.pagesPerSlice(); page++) {
            TmdbSearchResponse response = tmdbClient.discoverMovies(query, page);

            if (response.results() == null || response.results().isEmpty()) {
                return;
            }

            for (TmdbMovieSearchResultResponse result : response.results()) {
                if (result.id() != null) {
                    candidateIds.add(result.id());
                }

                if (reachedLimit(candidateIds)) {
                    return;
                }
            }

            if (page >= response.totalPages()) {
                return;
            }
        }
    }

    private boolean reachedLimit(Set<Long> candidateIds) {
        Integer maxCandidates = seedProperties.maxCandidates();
        return maxCandidates != null && candidateIds.size() >= maxCandidates;
    }
}
