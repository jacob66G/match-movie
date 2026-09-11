package io.github.jacob66g.matchmovie.movies.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public record MovieDiscoverCriteria(
        @Size(max = 100)
        String title,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate releaseDateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate releaseDateTo,
        @Min(1)
        Integer runtimeGTE,
        @Min(1)
        Integer runtimeLTE,
        @Size(max = 10)
        List<@NotBlank @Size(max = 50) String> genres
) {

    @AssertTrue(message = "releaseDateFrom must be before or equal to releaseDateTo")
    public boolean isReleaseDateRangeValid() {
        return releaseDateFrom == null
                || releaseDateTo == null
                || !releaseDateFrom.isAfter(releaseDateTo);
    }

    @AssertTrue(message = "runtimeGTE must be less than or equal to runtimeLTE")
    public boolean isRuntimeRangeValid() {
        return runtimeGTE == null
                || runtimeLTE == null
                || runtimeGTE <= runtimeLTE;
    }
}
