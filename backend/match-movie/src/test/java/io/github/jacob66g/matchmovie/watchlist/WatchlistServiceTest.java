package io.github.jacob66g.matchmovie.watchlist;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import io.github.jacob66g.matchmovie.movies.repository.MovieRepository;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchlistRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchlistItemResponse;
import io.github.jacob66g.matchmovie.watchlist.exception.WatchlistErrorCode;
import io.github.jacob66g.matchmovie.watchlist.model.UserMovieId;
import io.github.jacob66g.matchmovie.watchlist.model.WatchlistItem;
import io.github.jacob66g.matchmovie.watchlist.repository.WatchlistRepository;
import io.github.jacob66g.matchmovie.watchlist.service.WatchlistService;
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
class WatchlistServiceTest {

    private static final UUID TEST_USER_ID = UUID.fromString("6e9b93f1-62f0-41e9-9341-59a215bbcc46");
    private static final Long TEST_MOVIE_ID = 550L;
    private static final String TEST_TITLE = "Fight Club";

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private WatchlistService watchlistService;

    @Test
    void should_return_a_page_of_watchlist_items_of_the_given_user() {
        //given
        WatchlistItem item = new WatchlistItem(TEST_USER_ID, movie());
        Pageable pageable = PageRequest.of(0, 20);
        when(watchlistRepository.findAllById_UserId(TEST_USER_ID, pageable))
                .thenReturn(new PageImpl<>(List.of(item), pageable, 1));

        //when
        PageResponse<WatchlistItemResponse> watchlist = watchlistService.getWatchlist(TEST_USER_ID, pageable);

        //then
        assertThat(watchlist.content())
                .singleElement()
                .satisfies(response -> {
                    assertThat(response.movie().id()).isEqualTo(TEST_MOVIE_ID);
                    assertThat(response.movie().title()).isEqualTo(TEST_TITLE);
                });
        assertThat(watchlist.totalElements()).isEqualTo(1);
        assertThat(watchlist.page()).isZero();
        assertThat(watchlist.last()).isTrue();
    }

    @Test
    void should_save_item_when_movie_is_not_on_the_watchlist() {
        //given
        UserMovieId id = new UserMovieId(TEST_USER_ID, TEST_MOVIE_ID);
        when(watchlistRepository.existsById(id)).thenReturn(false);
        when(movieRepository.findById(TEST_MOVIE_ID)).thenReturn(Optional.of(movie()));
        when(watchlistRepository.save(any(WatchlistItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        WatchlistItemResponse response = watchlistService.addToWatchlist(TEST_USER_ID, new AddToWatchlistRequest(TEST_MOVIE_ID));

        //then
        ArgumentCaptor<WatchlistItem> itemCaptor = ArgumentCaptor.forClass(WatchlistItem.class);
        verify(watchlistRepository, times(1)).save(itemCaptor.capture());

        WatchlistItem saved = itemCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getMovie().getId()).isEqualTo(TEST_MOVIE_ID);
        assertThat(response.movie().id()).isEqualTo(TEST_MOVIE_ID);
    }

    @Test
    void should_throw_ApplicationException_when_movie_is_already_on_the_watchlist() {
        //given
        when(watchlistRepository.existsById(new UserMovieId(TEST_USER_ID, TEST_MOVIE_ID))).thenReturn(true);

        //when + then
        assertThatThrownBy(() -> watchlistService.addToWatchlist(TEST_USER_ID, new AddToWatchlistRequest(TEST_MOVIE_ID)))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(WatchlistErrorCode.ALREADY_IN_WATCHLIST);
        verify(watchlistRepository, never()).save(any());
    }

    @Test
    void should_throw_ApplicationException_when_movie_is_missing_from_the_catalogue() {
        //given
        when(watchlistRepository.existsById(new UserMovieId(TEST_USER_ID, TEST_MOVIE_ID))).thenReturn(false);
        when(movieRepository.findById(TEST_MOVIE_ID)).thenReturn(Optional.empty());

        //when + then
        assertThatThrownBy(() -> watchlistService.addToWatchlist(TEST_USER_ID, new AddToWatchlistRequest(TEST_MOVIE_ID)))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(MovieErrorCode.MOVIE_NOT_FOUND);
        verify(watchlistRepository, never()).save(any());
    }

    @Test
    void should_delete_item_when_movie_is_on_the_watchlist() {
        //given
        UserMovieId id = new UserMovieId(TEST_USER_ID, TEST_MOVIE_ID);
        when(watchlistRepository.existsById(id)).thenReturn(true);

        //when
        watchlistService.removeFromWatchlist(TEST_USER_ID, TEST_MOVIE_ID);

        //then
        verify(watchlistRepository, times(1)).deleteById(id);
    }

    @Test
    void should_throw_ApplicationException_when_removing_movie_that_is_not_on_the_watchlist() {
        //given
        when(watchlistRepository.existsById(new UserMovieId(TEST_USER_ID, TEST_MOVIE_ID))).thenReturn(false);

        //when + then
        assertThatThrownBy(() -> watchlistService.removeFromWatchlist(TEST_USER_ID, TEST_MOVIE_ID))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(WatchlistErrorCode.WATCHLIST_ITEM_NOT_FOUND);
        verify(watchlistRepository, never()).deleteById(any());
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
