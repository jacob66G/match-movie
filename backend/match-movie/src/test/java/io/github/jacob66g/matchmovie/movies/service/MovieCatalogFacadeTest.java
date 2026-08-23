package io.github.jacob66g.matchmovie.movies.service;

import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.client.TmdbClient;
import io.github.jacob66g.matchmovie.movies.client.mapper.TmdbSearchMapper;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.Set;

import static io.github.jacob66g.matchmovie.movies.MovieFactory.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieCatalogFacadeTest {

    @Mock
    private TmdbClient tmdbClient;

    @Mock
    private TmdbSearchMapper tmdbSearchMapper;

    @Mock
    private MovieCatalogService movieCatalogService;

    @InjectMocks
    private MovieCatalogFacade movieCatalogFacade;


    @Test
    void should_return_movie_without_call_to_tmdb() {
        //given
        when(movieCatalogService.findById(MOVIE_ID)).thenReturn(Optional.ofNullable(movie));

        //when
        Movie result = movieCatalogFacade.getOrImport(MOVIE_ID);

        //then
        assertThat(result).isSameAs(movie);
        verify(movieCatalogService, times(1)).findById(MOVIE_ID);
        verify(tmdbClient, never()).getMovieDetails(anyLong());
    }

    @Test
    void should_import_tmdb_movie_if_no_exists_in_catalog() {
        //given
        when(movieCatalogService.findById(MOVIE_ID)).thenReturn(Optional.empty());
        when(tmdbClient.getMovieDetails(MOVIE_ID)).thenReturn(tmdbMovieDetailsResponse);
        when(movieCatalogService.persistImported(tmdbMovieDetailsResponse, MovieOrigin.ON_DEMAND)).thenReturn(movie);

        //when
        Movie result = movieCatalogFacade.getOrImport(MOVIE_ID);

        //then
        assertThat(result).isSameAs(movie);
        verify(tmdbClient, times(1)).getMovieDetails(MOVIE_ID);
        verify(movieCatalogService, times(1)).persistImported(tmdbMovieDetailsResponse, MovieOrigin.ON_DEMAND);
    }

    @Test
    void should_throw_ApplicationException_when_integrity_violation_exception_and_no_found_movie() {
        //given
        when(movieCatalogService.findById(MOVIE_ID)).thenReturn(Optional.empty());
        when(tmdbClient.getMovieDetails(MOVIE_ID)).thenReturn(tmdbMovieDetailsResponse);
        when(movieCatalogService.persistImported(tmdbMovieDetailsResponse, MovieOrigin.ON_DEMAND)).thenThrow(DataIntegrityViolationException.class);

        //when + then
        assertThatThrownBy(() -> movieCatalogFacade.getOrImport(MOVIE_ID))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(MovieErrorCode.TMDB_INVALID_RESPONSE);
    }

    @Test
    void should_return_movie_when_integrity_violation_exception_and_found_movie() {
        //given
        when(movieCatalogService.findById(MOVIE_ID)).thenReturn(Optional.empty()).thenReturn(Optional.of(movie));
        when(tmdbClient.getMovieDetails(MOVIE_ID)).thenReturn(tmdbMovieDetailsResponse);
        when(movieCatalogService.persistImported(tmdbMovieDetailsResponse, MovieOrigin.ON_DEMAND)).thenThrow(DataIntegrityViolationException.class);

        //when
        Movie result = movieCatalogFacade.getOrImport(MOVIE_ID);

        //then
        assertThat(result).isSameAs(movie);
        verify(movieCatalogService, times(2)).findById(MOVIE_ID);
    }

    @Test
    void should_return_pageResponse_of_MovieSearchResponse() {
        //given
        when(tmdbClient.searchMovies(TITLE, 1)).thenReturn(tmdbSearchResponse);
        when(movieCatalogService.findCatalogIds(Set.of(MOVIE_ID))).thenReturn(Set.of());
        when(tmdbSearchMapper.toMovieIds(tmdbSearchResponse)).thenReturn(Set.of(MOVIE_ID));
        when(tmdbSearchMapper.toSearchPage(tmdbSearchResponse, Set.of())).thenReturn(searchPage);

        //when
        movieCatalogFacade.search(TITLE, 1);

        //then
        verify(tmdbClient, times(1)).searchMovies(TITLE, 1);
        verify(movieCatalogService, times(1)).findCatalogIds(Set.of(MOVIE_ID));
        verify(tmdbSearchMapper, times(1)).toSearchPage(tmdbSearchResponse, Set.of());
    }

}