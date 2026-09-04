package io.github.jacob66g.matchmovie.movies.seed;

import java.time.Duration;
import java.util.Set;

public record MovieSeedReport(
        int candidates,
        int toImport,
        int imported,
        int skipped,
        Set<Long> failedIds,
        Duration duration
) {

    public int alreadyInCatalog() {
        return candidates - toImport;
    }

    public double failureRate() {
        return toImport == 0 ? 0 : (double) failedIds.size() / toImport;
    }
}
