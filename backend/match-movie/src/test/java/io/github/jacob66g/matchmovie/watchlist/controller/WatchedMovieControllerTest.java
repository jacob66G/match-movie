package io.github.jacob66g.matchmovie.watchlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.watchlist.dto.AddToWatchedRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.UpdateWatchedMovieRequest;
import io.github.jacob66g.matchmovie.watchlist.dto.WatchedMovieResponse;
import io.github.jacob66g.matchmovie.watchlist.dto.sort.WatchedMovieSortField;
import io.github.jacob66g.matchmovie.watchlist.service.WatchedMovieFacade;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WatchedMovieController.class)
class WatchedMovieControllerTest {

    private static final Integer RATING = 10;
    private static final String REVIEW = "Good movie";
    private static final Instant WATCHED_AT = Instant.parse("2007-12-03T10:15:30.00Z");
    private static final UUID USER_ID = UUID.fromString("7b7f4432-dadf-4ab6-9646-5f96b90f30e3");
    private static final WatchedMovieResponse WATCHED_MOVIE_RESPONSE = new WatchedMovieResponse(
            movieSummaryResponse,
            RATING,
            REVIEW,
            WATCHED_AT
    );
    private static final PageResponse<WatchedMovieResponse> WATCHED_MOVIE_PAGE_RESPONSE = PageResponse.from(
            new PageImpl<>(List.of(WATCHED_MOVIE_RESPONSE))
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WatchedMovieFacade watchedMovieFacade;

    @Test
    void getWatchedMovie_should_return_200_for_valid_request() throws Exception {
        //given
        when(watchedMovieFacade.getWatchedMovie(USER_ID, MOVIE_ID)).thenReturn(WATCHED_MOVIE_RESPONSE);

        //when  + then
        mockMvc.perform(get("/api/users/me/watched/{movieId}", MOVIE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movie.title").value(TITLE))
                .andExpect(jsonPath("$.rating").value(RATING))
                .andExpect(jsonPath("$.review").value(REVIEW))
                .andExpect(jsonPath("$.watchedAt").value(WATCHED_AT.toString()));

        verify(watchedMovieFacade).getWatchedMovie(USER_ID, MOVIE_ID);
    }

    @Test
    void getWatchedMovie_should_return_400_when_movieId_is_not_positive() throws Exception {
        //given
        when(watchedMovieFacade.getWatchedMovie(any(), any())).thenReturn(WATCHED_MOVIE_RESPONSE);

        //when  + then
        mockMvc.perform(get("/api/users/me/watched/{movieId}", -1)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void getWatchedMovies_should_return_200_for_valid_request() throws Exception {
        //given
        when(watchedMovieFacade.getWatchedMovies(eq(USER_ID), any())).thenReturn(WATCHED_MOVIE_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/users/me/watched")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].review").value(REVIEW));

        verify(watchedMovieFacade).getWatchedMovies(eq(USER_ID), any());
    }


    @Test
    void getWatchedMovies_should_use_default_pageable() throws Exception {
        //given
        when(watchedMovieFacade.getWatchedMovies(eq(USER_ID), any())).thenReturn(WATCHED_MOVIE_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/users/me/watched")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(watchedMovieFacade).getWatchedMovies(eq(USER_ID), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort())
                .containsExactly(
                        Sort.Order.desc(WatchedMovieSortField.WATCHED_AT.entityPath())
                );
    }

    @Test
    void getWatchedMovies_should_pass_custom_pageable() throws Exception {
        //given
        when(watchedMovieFacade.getWatchedMovies(eq(USER_ID), any())).thenReturn(WATCHED_MOVIE_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/users/me/watched")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(watchedMovieFacade).getWatchedMovies(eq(USER_ID), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort())
                .containsExactly(
                        Sort.Order.asc(WatchedMovieSortField.TITLE.entityPath())
                );
    }

    @Test
    void getWatchedMovies_should_return_400_for_not_allowed_sort_field() throws Exception {
        mockMvc.perform(get("/api/users/me/watched")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .param("sort", "invalidField,asc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void addToWatched_should_return_201_for_valid_request() throws Exception {
        //given
        AddToWatchedRequest addToWatchedRequest = new AddToWatchedRequest(
                MOVIE_ID,
                RATING,
                REVIEW
        );

        when(watchedMovieFacade.addToWatched(USER_ID, addToWatchedRequest)).thenReturn(WATCHED_MOVIE_RESPONSE);

        //when + then
        mockMvc.perform(post("/api/users/me/watched")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToWatchedRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movie.title").value(TITLE))
                .andExpect(jsonPath("$.rating").value(RATING))
                .andExpect(jsonPath("$.review").value(REVIEW))
                .andExpect(header().string("Location", "/api/users/me/watched/" + MOVIE_ID));

        verify(watchedMovieFacade).addToWatched(USER_ID, addToWatchedRequest);
    }

    @Test
    void addToWatched_should_return_400_when_movieId_is_not_positive() throws Exception {
        //given
        AddToWatchedRequest addToWatchedRequest = new AddToWatchedRequest(
                -1L,
                RATING,
                REVIEW
        );

        //when + then
        mockMvc.perform(post("/api/users/me/watched")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToWatchedRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void addToWatched_should_return_400_when_movieId_is_null() throws Exception {
        //given
        AddToWatchedRequest addToWatchedRequest = new AddToWatchedRequest(
                null,
                RATING,
                REVIEW
        );

        //when + then
        mockMvc.perform(post("/api/users/me/watched")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToWatchedRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void addToWatched_should_return_400_when_rating_is_less_than_1() throws Exception {
        //given
        AddToWatchedRequest addToWatchedRequest = new AddToWatchedRequest(
                MOVIE_ID,
                -1,
                REVIEW
        );

        //when + then
        mockMvc.perform(post("/api/users/me/watched")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToWatchedRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void addToWatched_should_return_400_when_rating_is_greater_than_10() throws Exception {
        //given
        AddToWatchedRequest addToWatchedRequest = new AddToWatchedRequest(
                MOVIE_ID,
                11,
                REVIEW
        );

        //when + then
        mockMvc.perform(post("/api/users/me/watched")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToWatchedRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void addToWatched_should_return_400_when_review_exceeds_max_length() throws Exception {
        //given
        AddToWatchedRequest addToWatchedRequest = new AddToWatchedRequest(
                MOVIE_ID,
                RATING,
                ".".repeat(2001)
        );

        //when + then
        mockMvc.perform(post("/api/users/me/watched")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToWatchedRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void updateWatchedMovie_should_return_200_for_valid_request() throws Exception {
        //given
        UpdateWatchedMovieRequest updateWatchedMovieRequest = new UpdateWatchedMovieRequest(
                RATING,
                REVIEW
        );

        when(watchedMovieFacade.updateWatchedMovie(USER_ID, MOVIE_ID, updateWatchedMovieRequest)).thenReturn(WATCHED_MOVIE_RESPONSE);

        //when + then
        mockMvc.perform(patch("/api/users/me/watched/{movieId}", MOVIE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWatchedMovieRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movie.title").value(TITLE))
                .andExpect(jsonPath("$.rating").value(RATING))
                .andExpect(jsonPath("$.review").value(REVIEW));

        verify(watchedMovieFacade).updateWatchedMovie(USER_ID, MOVIE_ID, updateWatchedMovieRequest);
    }

    @Test
    void updateWatchedMovie_should_return_400_when_rating_is_less_than_1() throws Exception {
        //given
        UpdateWatchedMovieRequest updateWatchedMovieRequest = new UpdateWatchedMovieRequest(
                -1,
                REVIEW
        );

        //when + then
        mockMvc.perform(patch("/api/users/me/watched/{movieId}", MOVIE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWatchedMovieRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void updateWatchedMovie_should_return_400_when_rating_is_greater_than_10() throws Exception {
        //given
        UpdateWatchedMovieRequest updateWatchedMovieRequest = new UpdateWatchedMovieRequest(
                11,
                REVIEW
        );

        //when + then
        mockMvc.perform(patch("/api/users/me/watched/{movieId}", MOVIE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWatchedMovieRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void updateWatchedMovie_should_return_400_when_review_exceeds_max_length() throws Exception {
        //given
        UpdateWatchedMovieRequest updateWatchedMovieRequest = new UpdateWatchedMovieRequest(
                RATING,
                ".".repeat(2001)
        );

        //when + then
        mockMvc.perform(patch("/api/users/me/watched/{movieId}", MOVIE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWatchedMovieRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void updateWatchedMovie_should_return_400_when_movieId_is_not_positive() throws Exception {
        //given
        UpdateWatchedMovieRequest updateWatchedMovieRequest = new UpdateWatchedMovieRequest(
                RATING,
                REVIEW
        );

        //when + then
        mockMvc.perform(patch("/api/users/me/watched/{movieId}", -1)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWatchedMovieRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void updateWatchedMovie_should_return_400_when_no_field_is_changed() throws Exception {
        //given
        UpdateWatchedMovieRequest updateWatchedMovieRequest = new UpdateWatchedMovieRequest(
                null,
                null
        );

        //when + then
        mockMvc.perform(patch("/api/users/me/watched/{movieId}", MOVIE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWatchedMovieRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }

    @Test
    void removeFromWatched_should_return_204_for_valid_request() throws Exception {
        mockMvc.perform(delete("/api/users/me/watched/{movieId}", MOVIE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isNoContent());

        verify(watchedMovieFacade).removeFromWatched(USER_ID, MOVIE_ID);
    }

    @Test
    void removeFromWatched_should_return_400_when_movieId_is_not_positive() throws Exception {
        mockMvc.perform(delete("/api/users/me/watched/{movieId}", -1)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(watchedMovieFacade);
    }
}
