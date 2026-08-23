package io.github.jacob66g.matchmovie.movies.client.mapper;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;


final class TmdbUtils {

    private TmdbUtils() {
    }

    static String blankToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    static Integer normalizeRuntime(Integer runtime) {
        if (runtime == null || runtime <= 0) {
            return null;
        }
        return runtime;
    }

    static int normalizeVoteCount(Integer voteCount) {
        if (voteCount == null || voteCount < 0) {
            return 0;
        }
        return voteCount;
    }

    static BigDecimal normalizeVoteAverage(BigDecimal voteAverage) {
        if (voteAverage == null) {
            return null;
        }
        if (voteAverage.compareTo(BigDecimal.ZERO) < 0 || voteAverage.compareTo(BigDecimal.TEN) > 0) {
            return null;
        }
        return voteAverage;
    }
}
