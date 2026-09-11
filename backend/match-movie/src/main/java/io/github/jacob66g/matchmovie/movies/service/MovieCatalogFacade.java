package io.github.jacob66g.matchmovie.movies.service;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.client.TmdbClient;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbMovieDetailsResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbSearchResponse;
import io.github.jacob66g.matchmovie.movies.client.mapper.TmdbSearchMapper;
import io.github.jacob66g.matchmovie.movies.dto.MovieDiscoverCriteria;
import io.github.jacob66g.matchmovie.movies.dto.MovieSearchResponse;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
@RequiredArgsConstructor
public class MovieCatalogFacade {

    private final TmdbClient tmdbClient;
    private final TmdbSearchMapper tmdbSearchMapper;
    private final MovieCatalogService movieCatalogService;


    public Movie getOrImport(Long tmdbMovieId) {
        return movieCatalogService.findById(tmdbMovieId)
                .orElseGet(() -> importFromTmdb(tmdbMovieId));
    }

    public PageResponse<MovieSearchResponse> search(String query, int page) {
        TmdbSearchResponse tmdbResponse = tmdbClient.searchMovies(query, page);

        Set<Long> inCatalogIds = movieCatalogService.findCatalogIds(tmdbSearchMapper.toMovieIds(tmdbResponse));
        return tmdbSearchMapper.toSearchPage(tmdbResponse, inCatalogIds);
    }

    public PageResponse<MovieSearchResponse> discover(MovieDiscoverCriteria criteria, Pageable pageable) {
        return movieCatalogService.discoverCatalogMovies(criteria, pageable);
    }

    private Movie importFromTmdb(Long tmdbMovieId) {
        TmdbMovieDetailsResponse details = tmdbClient.getMovieDetails(tmdbMovieId, true);

        try {
            return movieCatalogService.persistImported(details, MovieOrigin.ON_DEMAND);
        } catch (DataIntegrityViolationException ex) {
            return movieCatalogService.findById(tmdbMovieId)
                    .orElseThrow(() -> new ApplicationException(MovieErrorCode.TMDB_INVALID_RESPONSE)
                            .causedBy(ex)
                            .with("movieId", tmdbMovieId)
                            .with("reason", "duplicate insert but row missing"));
        }
    }

}
