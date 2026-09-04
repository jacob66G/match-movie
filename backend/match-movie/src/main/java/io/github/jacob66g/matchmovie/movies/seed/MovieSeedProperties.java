package io.github.jacob66g.matchmovie.movies.seed;

import io.github.jacob66g.matchmovie.movies.client.TmdbClient;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "seed.movies")
public record MovieSeedProperties(

        @Min(EARLIEST_RELEASE_YEAR)
        int yearFrom,

        @Min(EARLIEST_RELEASE_YEAR)
        int yearTo,

        @Min(0)
        Integer minVoteCount,

        @Min(1)
        @Max(TmdbClient.MAX_PAGE)
        @DefaultValue("10")
        int pagesPerSlice,

        @Min(1)
        Integer maxCandidates,

        @Min(1)
        @DefaultValue("3")
        int maxAttempts,

        @Min(1)
        @DefaultValue("100")
        int progressLogEvery,

        @NotNull
        @DefaultValue("1s")
        Duration retryBackoff
) {

    static final int EARLIEST_RELEASE_YEAR = 1874;

    @AssertTrue(message = "seed.movies.yearTo must not be earlier than seed.movies.yearFrom")
    public boolean isYearRangeOrdered() {
        return yearTo >= yearFrom;
    }
}
