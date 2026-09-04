package io.github.jacob66g.matchmovie.movies.client.dto;

import java.time.LocalDate;

public record TmdbDiscoverQuery(
        LocalDate releaseDateFrom,
        LocalDate releaseDateTo,
        Integer minVoteCount,
        TmdbSortField sortBy
) {
}
