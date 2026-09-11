package io.github.jacob66g.matchmovie.watchlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchlistRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchlistItemResponse;
import io.github.jacob66g.matchmovie.watchlist.dto.sort.WatchlistSortField;
import io.github.jacob66g.matchmovie.watchlist.service.WatchlistFacade;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.github.jacob66g.matchmovie.movies.MovieFactory.MOVIE_ID;
import static io.github.jacob66g.matchmovie.movies.MovieFactory.TITLE;
import static io.github.jacob66g.matchmovie.movies.MovieFactory.movieSummaryResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WatchlistController.class)
class WatchlistControllerTest {

    private static final UUID USER_ID = UUID.fromString("7b7f4432-dadf-4ab6-9646-5f96b90f30e3");
    private static final Instant ADDED_AT = Instant.parse("2007-12-03T10:15:30.00Z");
    private static final WatchlistItemResponse WATCHLIST_RESPONSE = new WatchlistItemResponse(
            movieSummaryResponse,
            ADDED_AT
    );
    private static final PageResponse<WatchlistItemResponse> WATCHLIST_PAGE_RESPONSE = PageResponse.from(
            new PageImpl<>(List.of(WATCHLIST_RESPONSE))
    );

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WatchlistFacade watchlistFacade;

    @Test
    void getWatchlist_should_return_200_for_valid_request() throws Exception {
        //given
        when(watchlistFacade.getWatchlist(eq(USER_ID), any())).thenReturn(WATCHLIST_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/users/me/watchlist")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].movie.id").value(MOVIE_ID.toString()))
                .andExpect(jsonPath("$.content[0].addedAt").value(ADDED_AT.toString()));

        verify(watchlistFacade).getWatchlist(eq(USER_ID), any());
    }

    @Test
    void getWatchlist_should_use_default_pageable() throws Exception {
        //given
        when(watchlistFacade.getWatchlist(eq(USER_ID), any())).thenReturn(WATCHLIST_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/users/me/watchlist")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(watchlistFacade).getWatchlist(eq(USER_ID), captor.capture());

        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort()).containsExactly(
                Sort.Order.desc(WatchlistSortField.ADDED_AT.entityPath())
        );
    }

    @Test
    void getWatchlist_should_pass_custom_pageable() throws Exception {
        //given
        when(watchlistFacade.getWatchlist(eq(USER_ID), any())).thenReturn(WATCHLIST_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/users/me/watchlist")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(watchlistFacade).getWatchlist(eq(USER_ID), captor.capture());

        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort()).containsExactly(
                Sort.Order.asc(WatchlistSortField.TITLE.entityPath())
        );
    }

    @Test
    void getWatchlist_should_return_400_for_not_allowed_sort_field() throws Exception {
        mockMvc.perform(get("/api/users/me/watchlist")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .param("sort", "invalidField,asc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchlistFacade);
    }

    @Test
    void addToWatchlist_should_return_201_for_valid_request() throws Exception {
        //given
        AddToWatchlistRequest addToWatchlistRequest = new AddToWatchlistRequest(MOVIE_ID);
        when(watchlistFacade.addToWatchlist(USER_ID, addToWatchlistRequest)).thenReturn(WATCHLIST_RESPONSE);

        //when + then
        mockMvc.perform(post("/api/users/me/watchlist")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToWatchlistRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movie.title").value(TITLE))
                .andExpect(jsonPath("$.addedAt").value(ADDED_AT.toString()));

        verify(watchlistFacade).addToWatchlist(USER_ID, addToWatchlistRequest);
    }

    @Test
    void addToWatchlist_should_return_400_when_movieId_is_not_positive() throws Exception {
        //given
        AddToWatchlistRequest addToWatchlistRequest = new AddToWatchlistRequest(-1L);

        //when + then
        mockMvc.perform(post("/api/users/me/watchlist")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToWatchlistRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchlistFacade);
    }

    @Test
    void addToWatchlist_should_return_400_when_movieId_is_null() throws Exception {
        //given
        AddToWatchlistRequest addToWatchlistRequest = new AddToWatchlistRequest(null);

        //when + then
        mockMvc.perform(post("/api/users/me/watchlist")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToWatchlistRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchlistFacade);
    }

    @Test
    void removeFromWatchlist_should_return_204_for_valid_request() throws Exception {
        mockMvc.perform(delete("/api/users/me/watchlist/{movieId}", MOVIE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isNoContent());

        verify(watchlistFacade).removeFromWatchlist(USER_ID, MOVIE_ID);
    }

    @Test
    void removeFromWatchlist_should_return_400_when_movieId_is_not_positive() throws Exception {
        mockMvc.perform(delete("/api/users/me/watchlist/{movieId}", -1L)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchlistFacade);
    }
}
