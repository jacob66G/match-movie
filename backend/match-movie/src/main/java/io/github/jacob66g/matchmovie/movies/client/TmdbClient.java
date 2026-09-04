package io.github.jacob66g.matchmovie.movies.client;

import io.github.jacob66g.matchmovie.common.config.CacheConfig;
import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbDiscoverQuery;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbGenreListResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbMovieDetailsResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.TmdbSearchResponse;
import io.github.jacob66g.matchmovie.movies.client.mapper.TmdbQueryMapper;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class TmdbClient {

    public static final int MAX_PAGE = 500;

    private static final String CIRCUIT_BREAKER = "tmdb";

    private final RestClient tmdbRestClient;
    private final TmdbProperties tmdbProperties;

    public TmdbClient(@Qualifier("tmdbRestClient") RestClient tmdbRestClient, TmdbProperties tmdbProperties) {
        this.tmdbRestClient = tmdbRestClient;
        this.tmdbProperties = tmdbProperties;
    }

    public String language() {
        return tmdbProperties.language();
    }

    @Cacheable(
            cacheNames = CacheConfig.TMDB_MOVIE_DETAILS_CACHE,
            key = "#root.target.language() + ':' + #movieId",
            sync = true,
            condition = "#useCache"
    )
    @CircuitBreaker(name = CIRCUIT_BREAKER, fallbackMethod = "getMovieDetailsFallback")
    @RateLimiter(name = CIRCUIT_BREAKER)
    public TmdbMovieDetailsResponse getMovieDetails(long movieId, boolean useCache) {
        return requireBody(tmdbRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movieId}")
                        .queryParam("language", tmdbProperties.language())
                        .queryParam("append_to_response", "translations")
                        .build(movieId))
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ApplicationException(MovieErrorCode.TMDB_MOVIE_NOT_FOUND)
                            .with("movieId", movieId);
                })
                .body(TmdbMovieDetailsResponse.class));
    }

    @Cacheable(
            cacheNames = CacheConfig.TMDB_SEARCH_CACHE,
            key = "#root.target.language() + ':' + #query.trim().toLowerCase() + ':' + #page",
            sync = true
    )
    @CircuitBreaker(name = CIRCUIT_BREAKER, fallbackMethod = "searchMoviesFallback")
    @RateLimiter(name = CIRCUIT_BREAKER)
    public TmdbSearchResponse searchMovies(String query, int page) {
        return requireBody(tmdbRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("include_adult", false)
                        .queryParam("language", tmdbProperties.language())
                        .build())
                .retrieve()
                .body(TmdbSearchResponse.class));
    }

    @Cacheable(cacheNames = CacheConfig.TMDB_GENRES_CACHE, key = "#root.target.language()", sync = true)
    @CircuitBreaker(name = CIRCUIT_BREAKER, fallbackMethod = "listGenresFallback")
    @RateLimiter(name = CIRCUIT_BREAKER)
    public TmdbGenreListResponse listGenres() {
        return requireBody(tmdbRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/genre/movie/list")
                        .queryParam("language", tmdbProperties.language())
                        .build())
                .retrieve()
                .body(TmdbGenreListResponse.class));
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER, fallbackMethod = "discoverMoviesFallback")
    @RateLimiter(name = CIRCUIT_BREAKER)
    public TmdbSearchResponse discoverMovies(TmdbDiscoverQuery query, int page) {
        return requireBody(tmdbRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/discover/movie")
                        .queryParams(TmdbQueryMapper.toQueryParams(query))
                        .queryParam("page", page)
                        .queryParam("include_adult", false)
                        .queryParam("language", tmdbProperties.language())
                        .build())
                .retrieve()
                .body(TmdbSearchResponse.class));
    }

    private static <T> T requireBody(T body) {
        if (body == null) {
            throw new ApplicationException(MovieErrorCode.TMDB_INVALID_RESPONSE)
                    .with("reason", "null body");
        }
        return body;
    }

    @SuppressWarnings("unused")
    private TmdbMovieDetailsResponse getMovieDetailsFallback(long movieId, boolean useCache, Throwable cause) {
        throw toApplicationException(cause).with("movieId", movieId);
    }

    @SuppressWarnings("unused")
    private TmdbSearchResponse searchMoviesFallback(String query, int page, Throwable cause) {
        throw toApplicationException(cause).with("query", query).with("page", page);
    }

    @SuppressWarnings("unused")
    private TmdbSearchResponse discoverMoviesFallback(TmdbDiscoverQuery query, int page, Throwable cause) {
        throw toApplicationException(cause).with("query", query).with("page", page);
    }

    @SuppressWarnings("unused")
    private TmdbGenreListResponse listGenresFallback(Throwable cause) {
        throw toApplicationException(cause);
    }

    private static ApplicationException toApplicationException(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof ApplicationException applicationException) {
                return applicationException;
            }
            current = current.getCause();
        }
        return new ApplicationException(MovieErrorCode.TMDB_UNAVAILABLE).causedBy(cause);
    }
}
