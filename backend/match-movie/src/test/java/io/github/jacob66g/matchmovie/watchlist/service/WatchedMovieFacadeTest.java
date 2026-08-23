package io.github.jacob66g.matchmovie.watchlist.service;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import io.github.jacob66g.matchmovie.movies.service.MovieCatalogFacade;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchedRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.UpdateWatchedMovieRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchedMovieResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchedMovieFacadeTest {

    private static final UUID TEST_USER_ID = UUID.fromString("6e9b93f1-62f0-41e9-9341-59a215bbcc46");
    private static final Long TEST_MOVIE_ID = 550L;

    @Mock
    private MovieCatalogFacade movieCatalogFacade;

    @Mock
    private WatchedMovieService watchedMovieService;

    @InjectMocks
    private WatchedMovieFacade watchedMovieFacade;

    @Test
    void should_delegate_getting_a_single_watched_movie_to_the_service() {
        //given
        WatchedMovieResponse response = mock(WatchedMovieResponse.class);
        when(watchedMovieService.getWatchedMovie(TEST_USER_ID, TEST_MOVIE_ID)).thenReturn(response);

        //when
        WatchedMovieResponse result = watchedMovieFacade.getWatchedMovie(TEST_USER_ID, TEST_MOVIE_ID);

        //then
        assertThat(result).isSameAs(response);
        verifyNoInteractions(movieCatalogFacade);
    }

    @Test
    void should_delegate_getting_a_page_of_watched_movies_to_the_service() {
        //given
        Pageable pageable = PageRequest.of(0, 20);
        PageResponse<WatchedMovieResponse> page = new PageResponse<>(java.util.List.of(), 0, 20, 0, 0, true);
        when(watchedMovieService.getWatchedMovies(TEST_USER_ID, pageable)).thenReturn(page);

        //when
        PageResponse<WatchedMovieResponse> result = watchedMovieFacade.getWatchedMovies(TEST_USER_ID, pageable);

        //then
        assertThat(result).isSameAs(page);
    }

    @Test
    void should_import_the_movie_before_adding_it_to_watched() {
        //given
        AddToWatchedRequest request = new AddToWatchedRequest(TEST_MOVIE_ID, 8, "Great movie.");
        Movie movie = movie();
        WatchedMovieResponse response = mock(WatchedMovieResponse.class);

        when(movieCatalogFacade.getOrImport(TEST_MOVIE_ID)).thenReturn(movie);
        when(watchedMovieService.addToWatched(TEST_USER_ID, movie, request)).thenReturn(response);

        //when
        WatchedMovieResponse result = watchedMovieFacade.addToWatched(TEST_USER_ID, request);

        //then
        assertThat(result).isSameAs(response);
        verify(movieCatalogFacade, times(1)).getOrImport(TEST_MOVIE_ID);
        verify(watchedMovieService, times(1)).addToWatched(TEST_USER_ID, movie, request);
    }

    @Test
    void should_delegate_updating_a_watched_movie_to_the_service() {
        //given
        UpdateWatchedMovieRequest request = new UpdateWatchedMovieRequest(5, "Updated review");
        WatchedMovieResponse response = mock(WatchedMovieResponse.class);
        when(watchedMovieService.updateWatchedMovie(TEST_USER_ID, TEST_MOVIE_ID, request)).thenReturn(response);

        //when
        WatchedMovieResponse result = watchedMovieFacade.updateWatchedMovie(TEST_USER_ID, TEST_MOVIE_ID, request);

        //then
        assertThat(result).isSameAs(response);
        verifyNoInteractions(movieCatalogFacade);
    }

    @Test
    void should_delegate_removing_a_watched_movie_to_the_service() {
        //when
        watchedMovieFacade.removeFromWatched(TEST_USER_ID, TEST_MOVIE_ID);

        //then
        verify(watchedMovieService, times(1)).removeFromWatched(TEST_USER_ID, TEST_MOVIE_ID);
        verifyNoInteractions(movieCatalogFacade);
    }

    private static Movie movie() {
        return Movie.builder()
                .id(TEST_MOVIE_ID)
                .title("Fight Club")
                .releaseDate(LocalDate.of(1999, 10, 15))
                .voteAverage(new BigDecimal("8.433"))
                .voteCount(26279)
                .origin(MovieOrigin.SEED)
                .syncedAt(Instant.parse("2026-08-01T10:00:00Z"))
                .build();
    }
}
