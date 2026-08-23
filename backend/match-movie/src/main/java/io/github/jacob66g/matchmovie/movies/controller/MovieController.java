package io.github.jacob66g.matchmovie.movies.controller;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.movies.dto.MovieSearchRequest;
import io.github.jacob66g.matchmovie.movies.dto.MovieSearchResponse;
import io.github.jacob66g.matchmovie.movies.service.MovieCatalogFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieCatalogFacade movieCatalogFacade;

    @GetMapping
    public ResponseEntity<PageResponse<MovieSearchResponse>> searchMovie(@Valid @ModelAttribute MovieSearchRequest searchRequest) {
        return ResponseEntity.ok(movieCatalogFacade.search(searchRequest.query(), searchRequest.page()));
    }

}
