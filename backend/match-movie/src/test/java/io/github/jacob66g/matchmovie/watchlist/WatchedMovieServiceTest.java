package io.github.jacob66g.matchmovie.watchlist;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import io.github.jacob66g.matchmovie.movies.repository.MovieRepository;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchedRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.UpdateWatchedMovieRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchedMovieResponse;
import io.github.jacob66g.matchmovie.watchlist.exception.WatchedMovieErrorCode;
import io.github.jacob66g.matchmovie.watchlist.model.UserMovieId;
import io.github.jacob66g.matchmovie.watchlist.model.WatchedMovie;
import io.github.jacob66g.matchmovie.watchlist.repository.WatchedMovieRepository;
import io.github.jacob66g.matchmovie.watchlist.service.WatchedMovieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WatchedMovieServiceTest {

    private static final UUID TEST_USER_ID = UUID.fromString("6e9b93f1-62f0-41e9-9341-59a215bbcc46");
    private static final Long TEST_MOVIE_ID = 550L;
    private static final String TEST_TITLE = "Fight Club";
    private static final Integer TEST_RATING = 9;
    private static final String TEST_REVIEW = "Great movie.";

    @Mock
    private WatchedMovieRepository watchedMovieRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private WatchedMovieService watchedMovieService;

    @Test
    void should_return_a_page_of_watched_movies_of_the_given_user() {
        //given
        Pageable pageable = PageRequest.of(0, 20);
        when(watchedMovieRepository.findAllById_UserId(TEST_USER_ID, pageable))
                .thenReturn(new PageImpl<>(List.of(watchedMovie()), pageable, 1));

        //when
        PageResponse<WatchedMovieResponse> watched = watchedMovieService.getWatchedMovies(TEST_USER_ID, pageable);

        //then
        assertThat(watched.content())
                .singleElement()
                .satisfies(response -> {
                    assertThat(response.movie().id()).isEqualTo(TEST_MOVIE_ID);
                    assertThat(response.movie().title()).isEqualTo(TEST_TITLE);
                });
        assertThat(watched.totalElements()).isEqualTo(1);
    }

    @Test
    void should_throw_ApplicationException_when_watched_movie_does_not_exist() {
        //given
        when(watchedMovieRepository.findWithMovieById(userMovieId())).thenReturn(Optional.empty());

        //when + then
        assertThatThrownBy(() -> watchedMovieService.getWatchedMovie(TEST_USER_ID, TEST_MOVIE_ID))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(WatchedMovieErrorCode.WATCHED_MOVIE_NOT_FOUND);
    }

    @Test
    void should_save_movie_when_it_is_not_among_watched_movies() {
        //given
        when(watchedMovieRepository.existsById(userMovieId())).thenReturn(false);
        when(movieRepository.findById(TEST_MOVIE_ID)).thenReturn(Optional.of(movie()));
        when(watchedMovieRepository.save(any(WatchedMovie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        WatchedMovieResponse response = watchedMovieService.addToWatched(
                TEST_USER_ID, new AddToWatchedRequest(TEST_MOVIE_ID, TEST_RATING, TEST_REVIEW));

        //then
        ArgumentCaptor<WatchedMovie> captor = ArgumentCaptor.forClass(WatchedMovie.class);
        verify(watchedMovieRepository, times(1)).save(captor.capture());

        WatchedMovie saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(userMovieId());
        assertThat(saved.getMovie().getId()).isEqualTo(TEST_MOVIE_ID);
        assertThat(saved.getRating()).isEqualTo(TEST_RATING);
        assertThat(response.review()).isEqualTo(TEST_REVIEW);
    }

    @Test
    void should_throw_ApplicationException_when_movie_is_already_among_watched_movies() {
        //given
        when(watchedMovieRepository.existsById(userMovieId())).thenReturn(true);

        //when + then
        assertThatThrownBy(() -> watchedMovieService.addToWatched(
                TEST_USER_ID, new AddToWatchedRequest(TEST_MOVIE_ID, TEST_RATING, TEST_REVIEW)))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(WatchedMovieErrorCode.ALREADY_IN_WATCHED_MOVIES);
        verify(watchedMovieRepository, never()).save(any());
    }

    @Test
    void should_throw_ApplicationException_when_movie_is_missing_from_the_catalogue() {
        //given
        when(watchedMovieRepository.existsById(userMovieId())).thenReturn(false);
        when(movieRepository.findById(TEST_MOVIE_ID)).thenReturn(Optional.empty());

        //when + then
        assertThatThrownBy(() -> watchedMovieService.addToWatched(
                TEST_USER_ID, new AddToWatchedRequest(TEST_MOVIE_ID, TEST_RATING, TEST_REVIEW)))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(MovieErrorCode.MOVIE_NOT_FOUND);
        verify(watchedMovieRepository, never()).save(any());
    }

    @Test
    void should_update_only_the_fields_present_in_the_request() {
        //given
        when(watchedMovieRepository.findWithMovieById(userMovieId())).thenReturn(Optional.of(watchedMovie()));

        //when
        WatchedMovieResponse response = watchedMovieService.updateWatchedMovie(
                TEST_USER_ID, TEST_MOVIE_ID, new UpdateWatchedMovieRequest(3, null));

        //then
        assertThat(response.rating()).isEqualTo(3);
        assertThat(response.review()).isEqualTo(TEST_REVIEW);
    }

    @Test
    void should_delete_movie_when_it_is_among_watched_movies() {
        //given
        when(watchedMovieRepository.existsById(userMovieId())).thenReturn(true);

        //when
        watchedMovieService.removeFromWatched(TEST_USER_ID, TEST_MOVIE_ID);

        //then
        verify(watchedMovieRepository, times(1)).deleteById(userMovieId());
    }

    @Test
    void should_throw_ApplicationException_when_removing_movie_that_is_not_among_watched_movies() {
        //given
        when(watchedMovieRepository.existsById(userMovieId())).thenReturn(false);

        //when + then
        assertThatThrownBy(() -> watchedMovieService.removeFromWatched(TEST_USER_ID, TEST_MOVIE_ID))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(WatchedMovieErrorCode.WATCHED_MOVIE_NOT_FOUND);
        verify(watchedMovieRepository, never()).deleteById(any());
    }

    private UserMovieId userMovieId() {
        return new UserMovieId(TEST_USER_ID, TEST_MOVIE_ID);
    }

    private WatchedMovie watchedMovie() {
        return new WatchedMovie(TEST_USER_ID, movie(), TEST_RATING, TEST_REVIEW);
    }

    private static Movie movie() {
        return Movie.builder()
                .id(TEST_MOVIE_ID)
                .title(TEST_TITLE)
                .posterPath("/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg")
                .releaseDate(LocalDate.of(1999, 10, 15))
                .runtime(139)
                .voteAverage(new BigDecimal("8.433"))
                .voteCount(26279)
                .origin(MovieOrigin.SEED)
                .syncedAt(Instant.parse("2026-08-01T10:00:00Z"))
                .build();
    }
}
