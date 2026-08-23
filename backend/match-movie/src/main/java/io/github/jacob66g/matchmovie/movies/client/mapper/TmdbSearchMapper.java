package io.github.jacob66g.matchmovie.movies.client.mapper;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbMovieSearchResultResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbSearchResponse;
import io.github.jacob66g.matchmovie.movies.dto.MovieSearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.jacob66g.matchmovie.movies.client.mapper.TmdbUtils.blankToNull;
import static io.github.jacob66g.matchmovie.movies.client.mapper.TmdbUtils.normalizeVoteAverage;


@Component
public class TmdbSearchMapper {

    private static final int TMDB_PAGE_SIZE = 20;

    public Set<Long> toMovieIds(TmdbSearchResponse searchResponse) {
        if (searchResponse.results() == null) {
            return Set.of();
        }

        return searchResponse.results().stream()
                .filter(Objects::nonNull)
                .map(TmdbMovieSearchResultResponse::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public PageResponse<MovieSearchResponse> toSearchPage(TmdbSearchResponse searchResponse, Set<Long> inCatalogIds) {
        return new PageResponse<>(
                toSearchResults(searchResponse.results(), inCatalogIds),
                searchResponse.page(),
                TMDB_PAGE_SIZE,
                searchResponse.totalResults(),
                searchResponse.totalPages(),
                searchResponse.page() >= searchResponse.totalPages()
        );
    }

    private List<MovieSearchResponse> toSearchResults(List<TmdbMovieSearchResultResponse> items, Set<Long> inCatalogIds) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .filter(item -> item != null && item.id() != null && StringUtils.hasText(item.title()))
                .map(item -> new MovieSearchResponse(
                        item.id(),
                        item.title().trim(),
                        blankToNull(item.posterPath()),
                        item.releaseDate(),
                        normalizeVoteAverage(item.voteAverage()),
                        inCatalogIds.contains(item.id())
                ))
                .toList();
    }
}
