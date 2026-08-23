package io.github.jacob66g.matchmovie.watchlist.service;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import io.github.jacob66g.matchmovie.movies.service.MovieCatalogFacade;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchlistRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchlistItemResponse;
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
class WatchlistFacadeTest {

    private static final UUID TEST_USER_ID = UUID.fromString("6e9b93f1-62f0-41e9-9341-59a215bbcc46");
    private static final Long TEST_MOVIE_ID = 550L;

    @Mock
    private MovieCatalogFacade movieCatalogFacade;

    @Mock
    private WatchlistService watchlistService;

    @InjectMocks
    private WatchlistFacade watchlistFacade;

    @Test
    void should_delegate_getting_a_page_of_the_watchlist_to_the_service() {
        //given
        Pageable pageable = PageRequest.of(0, 20);
        PageResponse<WatchlistItemResponse> page = new PageResponse<>(java.util.List.of(), 0, 20, 0, 0, true);
        when(watchlistService.getWatchlist(TEST_USER_ID, pageable)).thenReturn(page);

        //when
        PageResponse<WatchlistItemResponse> result = watchlistFacade.getWatchlist(TEST_USER_ID, pageable);

        //then
        assertThat(result).isSameAs(page);
        verifyNoInteractions(movieCatalogFacade);
    }

    @Test
    void should_import_the_movie_before_adding_it_to_the_watchlist() {
        //given
        AddToWatchlistRequest request = new AddToWatchlistRequest(TEST_MOVIE_ID);
        Movie movie = movie();
        WatchlistItemResponse response = mock(WatchlistItemResponse.class);

        when(movieCatalogFacade.getOrImport(TEST_MOVIE_ID)).thenReturn(movie);
        when(watchlistService.addToWatchlist(TEST_USER_ID, movie)).thenReturn(response);

        //when
        WatchlistItemResponse result = watchlistFacade.addToWatchlist(TEST_USER_ID, request);

        //then
        assertThat(result).isSameAs(response);
        verify(movieCatalogFacade, times(1)).getOrImport(TEST_MOVIE_ID);
        verify(watchlistService, times(1)).addToWatchlist(TEST_USER_ID, movie);
    }

    @Test
    void should_delegate_removing_an_item_from_the_watchlist_to_the_service() {
        //when
        watchlistFacade.removeFromWatchlist(TEST_USER_ID, TEST_MOVIE_ID);

        //then
        verify(watchlistService, times(1)).removeFromWatchlist(TEST_USER_ID, TEST_MOVIE_ID);
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
