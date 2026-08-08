package io.github.jacob66g.matchmovie.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String TMDB_MOVIE_DETAILS_CACHE = "tmdbMovieDetails";
    public static final String TMDB_SEARCH_CACHE = "tmdbSearch";
    public static final String TMDB_GENRES_CACHE = "tmdbGenres";

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache(TMDB_MOVIE_DETAILS_CACHE, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(24))
                .maximumSize(10_000)
                .scheduler(Scheduler.systemScheduler())
                .recordStats()
                .build());

        cacheManager.registerCustomCache(TMDB_SEARCH_CACHE, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(24))
                .maximumSize(10_000)
                .scheduler(Scheduler.systemScheduler())
                .recordStats()
                .build());

        cacheManager.registerCustomCache(TMDB_GENRES_CACHE, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofDays(30))
                .maximumSize(100)
                .scheduler(Scheduler.systemScheduler())
                .recordStats()
                .build());

        return cacheManager;
    }
}
