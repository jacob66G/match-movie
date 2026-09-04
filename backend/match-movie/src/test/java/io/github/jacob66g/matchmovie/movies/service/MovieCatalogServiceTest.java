package io.github.jacob66g.matchmovie.movies.service;

import io.github.jacob66g.matchmovie.movies.client.mapper.TmdbCatalogMapper;
import io.github.jacob66g.matchmovie.movies.model.Genre;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import io.github.jacob66g.matchmovie.movies.repository.GenreRepository;
import io.github.jacob66g.matchmovie.movies.repository.MovieRepository;
import io.github.jacob66g.matchmovie.movies.repository.MovieTranslationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static io.github.jacob66g.matchmovie.movies.MovieFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieCatalogServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private MovieTranslationRepository movieTranslationRepository;

    @Mock
    private TmdbCatalogMapper tmdbCatalogMapper;

    @InjectMocks
    private MovieCatalogService movieCatalogService;

    @Test
    void should_persist_movie_and_translations_when_no_exists() {
        //given
        Set<Long> genreIds = Set.of(COMEDY_GENRE_ID, THRILLER_GENRE_ID, HORROR_GENRE_ID);

        when(tmdbCatalogMapper.toMovie(tmdbMovieDetailsResponse, MovieOrigin.ON_DEMAND)).thenReturn(movie);
        when(tmdbCatalogMapper.toGenres(tmdbMovieDetailsResponse.genres())).thenReturn(genres);
        when(genreRepository.findAllById(genreIds)).thenReturn(genres);
        when(tmdbCatalogMapper.toTranslations(tmdbMovieDetailsResponse.translations(), movie)).thenReturn(movieTranslations);

        //when
        Movie result = movieCatalogService.persistImported(tmdbMovieDetailsResponse, MovieOrigin.ON_DEMAND);

        //then
        verify(genreRepository, never()).insertIfAbsent(any(), any());
        verify(movieRepository).save(movie);
        verify(movieTranslationRepository).saveAll(movieTranslations);

        assertThat(result).isSameAs(movie);
        assertThat(result.getId()).isEqualTo(MOVIE_ID);
        assertThat(result.getOrigin()).isEqualTo(MovieOrigin.ON_DEMAND);
        assertThat(result.getGenres()).containsExactlyInAnyOrderElementsOf(genres);
    }

    @Test
    void should_insert_genres_absent_from_the_catalog_in_id_order() {
        //given
        Set<Long> genreIds = Set.of(COMEDY_GENRE_ID, THRILLER_GENRE_ID, HORROR_GENRE_ID);
        List<Genre> alreadyStored = List.of(new Genre(COMEDY_GENRE_ID, "Comedy"));

        when(tmdbCatalogMapper.toMovie(tmdbMovieDetailsResponse, MovieOrigin.ON_DEMAND)).thenReturn(movie);
        when(tmdbCatalogMapper.toGenres(tmdbMovieDetailsResponse.genres())).thenReturn(genres);
        when(genreRepository.findAllById(genreIds)).thenReturn(alreadyStored).thenReturn(genres);
        when(tmdbCatalogMapper.toTranslations(tmdbMovieDetailsResponse.translations(), movie)).thenReturn(movieTranslations);

        //when
        Movie result = movieCatalogService.persistImported(tmdbMovieDetailsResponse, MovieOrigin.ON_DEMAND);

        //then
        InOrder inOrder = inOrder(genreRepository);
        inOrder.verify(genreRepository).insertIfAbsent(HORROR_GENRE_ID, "Horror");
        inOrder.verify(genreRepository).insertIfAbsent(THRILLER_GENRE_ID, "Thriller");

        verify(genreRepository, never()).insertIfAbsent(COMEDY_GENRE_ID, "Comedy");
        verify(movieRepository).save(movie);

        assertThat(result.getGenres()).containsExactlyInAnyOrderElementsOf(genres);
    }

    @Test
    void should_return_the_subset_of_ids_already_in_the_catalog() {
        //given
        Set<Long> tmdbIds = Set.of(MOVIE_ID, SECOND_MOVIE_ID);
        when(movieRepository.findExistingIds(tmdbIds)).thenReturn(Set.of(MOVIE_ID));

        //when
        Set<Long> inCatalogIds = movieCatalogService.findCatalogIds(tmdbIds);

        //then
        assertThat(inCatalogIds).containsExactly(MOVIE_ID);
    }

    @Test
    void should_not_query_the_catalog_when_there_are_no_ids_to_check() {
        //when
        Set<Long> inCatalogIds = movieCatalogService.findCatalogIds(Set.of());

        //then
        assertThat(inCatalogIds).isEmpty();
        verify(movieRepository, never()).findExistingIds(any());
    }

}
