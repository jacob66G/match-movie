package io.github.jacob66g.matchmovie.movies.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MovieSeedRunner implements ApplicationRunner {

    private static final double MAX_ACCEPTABLE_FAILURE_RATE = 0.05;
    private static final int MAX_LOGGED_FAILED_IDS = 100;

    private final ApplicationContext context;
    private final MovieSeedService movieSeedService;

    @Override
    public void run(ApplicationArguments args) {
        MovieSeedReport report = movieSeedService.seed();

        log.info("Seed finished in {}s: {} candidates, {} already in catalog, {} to import "
                        + "-> {} imported, {} skipped, {} failed",
                report.duration().toSeconds(),
                report.candidates(),
                report.alreadyInCatalog(),
                report.toImport(),
                report.imported(),
                report.skipped(),
                report.failedIds().size());

        logFailedIds(report);

        System.exit(SpringApplication.exit(context, () -> exitCode(report)));
    }

    private void logFailedIds(MovieSeedReport report) {
        if (report.failedIds().isEmpty()) {
            return;
        }

        List<Long> sample = report.failedIds().stream().limit(MAX_LOGGED_FAILED_IDS).toList();
        log.warn("{} movies could not be imported. Re-running the seed retries them, "
                        + "because already imported movies are skipped. First {}: {}",
                report.failedIds().size(), sample.size(), sample);
    }

    private int exitCode(MovieSeedReport report) {
        if (report.failureRate() <= MAX_ACCEPTABLE_FAILURE_RATE) {
            return 0;
        }

        log.error("Seed failure rate {}% exceeds the accepted {}%",
                String.format("%.2f", report.failureRate() * 100),
                String.format("%.2f", MAX_ACCEPTABLE_FAILURE_RATE * 100));
        return 1;
    }
}
