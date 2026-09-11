package io.github.jacob66g.matchmovie.movies.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;


public record MovieSearchCriteria(
        @NotBlank
        String query,
        @Min(1)
        @Max(500)
        Integer page
) {

    private static final int FIRST_PAGE = 1;

    public MovieSearchCriteria {
        page = page == null ? FIRST_PAGE : page;
    }
}
