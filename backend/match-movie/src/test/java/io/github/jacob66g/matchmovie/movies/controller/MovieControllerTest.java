package io.github.jacob66g.matchmovie.movies.controller;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.movies.dto.MovieDiscoverCriteria;
import io.github.jacob66g.matchmovie.movies.dto.MovieSearchResponse;
import io.github.jacob66g.matchmovie.movies.dto.sort.MovieSortField;
import io.github.jacob66g.matchmovie.movies.service.MovieCatalogFacade;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static io.github.jacob66g.matchmovie.movies.MovieFactory.TITLE;
import static io.github.jacob66g.matchmovie.movies.MovieFactory.movieSearchResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    private static final PageResponse<MovieSearchResponse> MOVIE_SEARCH_RESPONSE_PAGE_RESPONSE = PageResponse.from(
            new PageImpl<>(List.of(movieSearchResponse()))
    );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieCatalogFacade movieCatalogFacade;

    @Test
    @WithMockUser
    void searchMovie_should_return_200_for_valid_request() throws Exception {
        //given
        when(movieCatalogFacade.search(TITLE, 1)).thenReturn(MOVIE_SEARCH_RESPONSE_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/movies/search")
                        .param("query", TITLE)
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].title").value(TITLE));

        verify(movieCatalogFacade).search(TITLE, 1);
    }

    @Test
    @WithMockUser
    void searchMovie_should_return_400_when_query_is_blank() throws Exception {
        mockMvc.perform(get("/api/movies/search")
                        .param("query", "")
                        .param("page", "1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieCatalogFacade);
    }

    @Test
    @WithMockUser
    void searchMovie_should_return_400_when_page_is_less_than_1() throws Exception {
        mockMvc.perform(get("/api/movies/search")
                        .param("query", "Star Wars")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieCatalogFacade);
    }

    @Test
    @WithMockUser
    void searchMovie_should_return_400_when_page_is_greater_than_500() throws Exception {
        mockMvc.perform(get("/api/movies/search")
                        .param("query", "Star Wars")
                        .param("page", "501"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieCatalogFacade);
    }

    @Test
    @WithMockUser
    void discoverMovie_should_return_200_for_valid_request() throws Exception {
        //given
        when(movieCatalogFacade.discover(any(), any())).thenReturn(MOVIE_SEARCH_RESPONSE_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/movies/discover")
                        .param("title", "Star Wars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].title").value(TITLE));

        verify(movieCatalogFacade).discover(any(), any());
    }

    @Test
    @WithMockUser
    void discoverMovie_should_use_default_pageable() throws Exception {
        //given
        when(movieCatalogFacade.discover(any(), any())).thenReturn(MOVIE_SEARCH_RESPONSE_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/movies/discover")
                        .param("title", "Star Wars"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(movieCatalogFacade).discover(any(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort())
                .containsExactly(
                        Sort.Order.desc(MovieSortField.POPULARITY.entityPath())
                );
    }

    @Test
    @WithMockUser
    void discoverMovie_should_pass_custom_pageable() throws Exception {
        //given
        when(movieCatalogFacade.discover(any(), any())).thenReturn(MOVIE_SEARCH_RESPONSE_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/movies/discover")
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "runtime,asc")
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(movieCatalogFacade)
                .discover(any(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber())
                .isEqualTo(1);

        assertThat(pageable.getPageSize())
                .isEqualTo(10);

        assertThat(pageable.getSort())
                .containsExactly(
                        Sort.Order.asc(MovieSortField.RUNTIME.entityPath())
                );
    }

    @Test
    @WithMockUser
    void discoverMovie_should_bind_query_params_to_criteria() throws Exception {
        //given
        when(movieCatalogFacade.discover(any(), any())).thenReturn(MOVIE_SEARCH_RESPONSE_PAGE_RESPONSE);

        //when + then
        mockMvc.perform(get("/api/movies/discover")
                        .param("title", "avatar")
                        .param("releaseDateFrom", "2010-01-01")
                        .param("releaseDateTo", "2025-01-01")
                        .param("runtimeGTE", "100")
                        .param("runtimeLTE", "200")
                        .param("genres", "Action, Drama")
                )
                .andExpect(status().isOk());

        ArgumentCaptor<MovieDiscoverCriteria> criteriaCaptor = ArgumentCaptor.forClass(MovieDiscoverCriteria.class);

        verify(movieCatalogFacade).discover(criteriaCaptor.capture(), any());

        MovieDiscoverCriteria criteria = criteriaCaptor.getValue();
        assertThat(criteria.title()).isEqualTo("avatar");
        assertThat(criteria.releaseDateFrom()).isEqualTo(LocalDate.of(2010, 1, 1));
        assertThat(criteria.releaseDateTo()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(criteria.runtimeGTE()).isEqualTo(100);
        assertThat(criteria.runtimeLTE()).isEqualTo(200);
        assertThat(criteria.genres()).containsExactly("Action", "Drama");
    }

    @Test
    @WithMockUser
    void discoverMovie_should_return_400_for_not_allowed_sort_field() throws Exception {
        mockMvc.perform(get("/api/movies/discover")
                        .param("sort", "invalidField,asc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieCatalogFacade);
    }

    @Test
    @WithMockUser
    void discoverMovie_should_return_400_for_invalid_date_format() throws Exception {
        mockMvc.perform(get("/api/movies/discover")
                        .param("releaseDateFrom", "01-01-2011"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieCatalogFacade);
    }

    @Test
    @WithMockUser
    void discoverMovie_should_return_400_when_runtime_gte_is_invalid() throws Exception {
        mockMvc.perform(get("/api/movies/discover")
                        .param("runtimeGTE", "invalid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieCatalogFacade);
    }

    @Test
    @WithMockUser
    void discoverMovie_should_return_400_when_runtime_lte_is_invalid() throws Exception {
        mockMvc.perform(get("/api/movies/discover")
                        .param("runtimeLTE", "invalid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieCatalogFacade);
    }


    @Test
    @WithMockUser
    void discoverMovie_should_return_400_when_release_date_from_is_after_to() throws Exception {
        mockMvc.perform(get("/api/movies/discover")
                        .param("releaseDateFrom", "2026-01-01")
                        .param("releaseDateTo", "2025-01-01"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieCatalogFacade);
    }

    @Test
    @WithMockUser
    void discoverMovie_should_return_400_when_runtime_gte_is_greater_than_lte() throws Exception {
        mockMvc.perform(get("/api/movies/discover")
                        .param("runtimeGTE", "100")
                        .param("runtimeLTE", "1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(movieCatalogFacade);
    }

}
