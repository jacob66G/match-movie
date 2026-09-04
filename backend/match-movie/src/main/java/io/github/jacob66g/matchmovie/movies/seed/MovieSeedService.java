package io.github.jacob66g.matchmovie.movies.seed;

import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.client.TmdbClient;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbMovieDetailsResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbMovieStatus;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import io.github.jacob66g.matchmovie.movies.service.MovieCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class MovieSeedService {

    private static final int CATALOG_LOOKUP_BATCH_SIZE = 1000;

    private final MovieSeedProperties seedProperties;
    private final MovieSeedPlanner seedPlanner;
    private final MovieCandidateCollector candidateCollector;
    private final MovieCatalogService movieCatalogService;
    private final TmdbClient tmdbClient;

    public MovieSeedReport seed() {
        Instant startedAt = Instant.now();

        Set<Long> candidateIds = candidateCollector.collect(seedPlanner.plan());
        Set<Long> idsToImport = findIdsMissingFromCatalog(candidateIds);

        log.info("Discovered {} candidates, {} already in the catalog, {} to import",
                candidateIds.size(), candidateIds.size() - idsToImport.size(), idsToImport.size());

        int imported = 0;
        int skipped = 0;
        Set<Long> failedIds = new LinkedHashSet<>();

        for (Long movieId : idsToImport) {
            if (Thread.currentThread().isInterrupted()) {
                log.warn("Seed interrupted after processing {} of {} movies",
                        imported + skipped + failedIds.size(), idsToImport.size());
                break;
            }

            switch (importMovie(movieId)) {
                case IMPORTED -> imported++;
                case SKIPPED -> skipped++;
                case FAILED -> failedIds.add(movieId);
            }

            int processed = imported + skipped + failedIds.size();
            if (processed % seedProperties.progressLogEvery() == 0) {
                log.info("Progress: {}/{} processed ({} imported, {} skipped, {} failed)",
                        processed, idsToImport.size(), imported, skipped, failedIds.size());
            }
        }

        return new MovieSeedReport(
                candidateIds.size(),
                idsToImport.size(),
                imported,
                skipped,
                failedIds,
                Duration.between(startedAt, Instant.now())
        );
    }

    private ImportOutcome importMovie(long movieId) {
        for (int attempt = 1; attempt <= seedProperties.maxAttempts(); attempt++) {
            try {
                TmdbMovieDetailsResponse details = tmdbClient.getMovieDetails(movieId, false);

                if (!isEligibleForCatalog(details)) {
                    log.debug("Movie {} does not meet the seed quality criteria", movieId);
                    return ImportOutcome.SKIPPED;
                }

                movieCatalogService.persistImported(details, MovieOrigin.SEED);
                return ImportOutcome.IMPORTED;

            } catch (DataIntegrityViolationException ex) {
                log.debug("Movie {} was inserted concurrently, nothing to do", movieId);
                return ImportOutcome.SKIPPED;

            } catch (RuntimeException ex) {
                if (isMissingInTmdb(ex)) {
                    log.debug("Movie {} no longer exists in TMDB", movieId);
                    return ImportOutcome.SKIPPED;
                }
                if (!backOffBeforeRetry(movieId, attempt, ex)) {
                    return ImportOutcome.FAILED;
                }
            }
        }

        return ImportOutcome.FAILED;
    }

    private boolean isEligibleForCatalog(TmdbMovieDetailsResponse details) {
        return details.id() != null
                && (StringUtils.hasText(details.title()) || StringUtils.hasText(details.originalTitle()))
                && StringUtils.hasText(details.overview())
                && TmdbMovieStatus.RELEASED.matches(details.status());
    }

    private boolean backOffBeforeRetry(long movieId, int attempt, RuntimeException cause) {
        if (attempt >= seedProperties.maxAttempts()) {
            log.warn("Giving up on movie {} after {} attempts: {}", movieId, attempt, cause.toString());
            return false;
        }

        log.debug("Attempt {} for movie {} failed ({}), retrying", attempt, movieId, cause.toString());

        try {
            Thread.sleep(seedProperties.retryBackoff().multipliedBy(1L << (attempt - 1)));
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static boolean isMissingInTmdb(RuntimeException ex) {
        return ex instanceof ApplicationException applicationException
                && applicationException.getErrorCode() == MovieErrorCode.TMDB_MOVIE_NOT_FOUND;
    }

    private Set<Long> findIdsMissingFromCatalog(Set<Long> candidateIds) {
        Set<Long> existingIds = new HashSet<>();
        Set<Long> batch = HashSet.newHashSet(CATALOG_LOOKUP_BATCH_SIZE);

        for (Long candidateId : candidateIds) {
            batch.add(candidateId);

            if (batch.size() == CATALOG_LOOKUP_BATCH_SIZE) {
                existingIds.addAll(movieCatalogService.findCatalogIds(batch));
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            existingIds.addAll(movieCatalogService.findCatalogIds(batch));
        }

        return candidateIds.stream()
                .filter(candidateId -> !existingIds.contains(candidateId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private enum ImportOutcome {
        IMPORTED,
        SKIPPED,
        FAILED
    }
}
