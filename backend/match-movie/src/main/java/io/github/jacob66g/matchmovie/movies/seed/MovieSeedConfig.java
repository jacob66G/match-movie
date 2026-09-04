package io.github.jacob66g.matchmovie.movies.seed;

import io.github.jacob66g.matchmovie.movies.client.TmdbClient;
import io.github.jacob66g.matchmovie.movies.service.MovieCatalogService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("seed")
@EnableConfigurationProperties(MovieSeedProperties.class)
class MovieSeedConfig {

    @Bean
    MovieSeedPlanner movieSeedPlanner(MovieSeedProperties seedProperties) {
        return new MovieSeedPlanner(seedProperties);
    }

    @Bean
    MovieCandidateCollector movieCandidateCollector(TmdbClient tmdbClient, MovieSeedProperties seedProperties) {
        return new MovieCandidateCollector(tmdbClient, seedProperties);
    }

    @Bean
    MovieSeedService movieSeedService(
            MovieSeedProperties seedProperties,
            MovieSeedPlanner seedPlanner,
            MovieCandidateCollector candidateCollector,
            MovieCatalogService movieCatalogService,
            TmdbClient tmdbClient) {
        return new MovieSeedService(
                seedProperties, seedPlanner, candidateCollector, movieCatalogService, tmdbClient);
    }

    @Bean
    MovieSeedRunner movieSeedRunner(ApplicationContext context, MovieSeedService movieSeedService) {
        return new MovieSeedRunner(context, movieSeedService);
    }
}
