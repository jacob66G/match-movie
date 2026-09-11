package io.github.jacob66g.matchmovie.movies.controller;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.common.sort.SortWhitelist;
import io.github.jacob66g.matchmovie.movies.dto.MovieDiscoverCriteria;
import io.github.jacob66g.matchmovie.movies.dto.MovieSearchCriteria;
import io.github.jacob66g.matchmovie.movies.dto.MovieSearchResponse;
import io.github.jacob66g.matchmovie.movies.dto.sort.MovieSortField;
import io.github.jacob66g.matchmovie.movies.service.MovieCatalogFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private static final SortWhitelist<MovieSortField> SORT_WHITELIST = SortWhitelist.of(MovieSortField.class);

    private final MovieCatalogFacade movieCatalogFacade;

    @GetMapping("/search")
    public ResponseEntity<PageResponse<MovieSearchResponse>> searchMovie(@Valid MovieSearchCriteria searchRequest) {
        return ResponseEntity.ok(movieCatalogFacade.search(searchRequest.query(), searchRequest.page()));
    }

    @GetMapping("/discover")
    public ResponseEntity<PageResponse<MovieSearchResponse>> discoverCatalogMovie(
            @Valid MovieDiscoverCriteria criteria,
            @PageableDefault(size = 20, sort = "popularity", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(movieCatalogFacade.discover(criteria, SORT_WHITELIST.sanitize(pageable)));
    }

}
