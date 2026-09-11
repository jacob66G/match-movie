package io.github.jacob66g.matchmovie.common.sort;

import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.common.exception.errorcode.CommonErrorCode;
import io.github.jacob66g.matchmovie.watchlist.dto.sort.WatchlistSortField;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortWhitelistTest {

    private final SortWhitelist<WatchlistSortField> whitelist = SortWhitelist.of(WatchlistSortField.class);

    @Test
    void should_translate_api_field_name_to_entity_property_path() {
        //given
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "movieId"));

        //when
        Pageable sanitized = whitelist.sanitize(pageable);

        //then
        Sort.Order order = sanitized.getSort().getOrderFor("id.movieId");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void should_preserve_page_number_and_size() {
        //given
        Pageable pageable = PageRequest.of(3, 15, Sort.by("addedAt"));

        //when
        Pageable sanitized = whitelist.sanitize(pageable);

        //then
        assertThat(sanitized.getPageNumber()).isEqualTo(3);
        assertThat(sanitized.getPageSize()).isEqualTo(15);
    }

    @Test
    void should_accept_api_field_name_regardless_of_case() {
        //given
        Pageable pageable = PageRequest.of(0, 20, Sort.by("ADDEDAT"));

        //when
        Pageable sanitized = whitelist.sanitize(pageable);

        //then
        assertThat(sanitized.getSort().getOrderFor("addedAt")).isNotNull();
    }

    @Test
    void should_throw_ApplicationException_for_a_field_outside_the_whitelist() {
        //given
        Pageable pageable = PageRequest.of(0, 20, Sort.by("review"));

        //when + then
        assertThatThrownBy(() -> whitelist.sanitize(pageable))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(CommonErrorCode.INVALID_SORT_PROPERTY);
    }

    @Test
    void should_pass_through_an_unsorted_pageable() {
        //given
        Pageable pageable = PageRequest.of(0, 20);

        //when
        Pageable sanitized = whitelist.sanitize(pageable);

        //then
        assertThat(sanitized.getSort().isUnsorted()).isTrue();
    }
}
