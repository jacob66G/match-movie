package io.github.jacob66g.matchmovie.movies;

import io.github.jacob66g.matchmovie.common.dto.PageResponse;
import io.github.jacob66g.matchmovie.movies.client.dto.*;
import io.github.jacob66g.matchmovie.movies.dto.MovieSearchResponse;
import io.github.jacob66g.matchmovie.movies.dto.MovieSummaryResponse;
import io.github.jacob66g.matchmovie.movies.model.Genre;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import io.github.jacob66g.matchmovie.movies.model.MovieTranslation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class MovieFactory {

    public static final Long MOVIE_ID = 550L;
    public static final Long SECOND_MOVIE_ID = 680L;
    public static final String TITLE = "Fight Club";
    public static final String SECOND_TITLE = "Pulp Fiction";
    public static final String ORIGINAL_TITLE = "Fight Club";
    public static final String ORIGINAL_LANGUAGE = "en";
    public static final String OVERVIEW = "An insomniac office worker and a soap maker form an underground fight club.";
    public static final String TAGLINE = "Mischief. Mayhem. Soap.";
    public static final String POSTER_PATH = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg";
    public static final String BACKDROP_PATH = "/fCayJrkfRaCRCTh8GqN30f8oyQF.jpg";
    public static final String IMDB_ID = "tt0137523";
    public static final String STATUS = "Released";
    public static final LocalDate RELEASE_DATE = LocalDate.of(1999, 10, 15);
    public static final Integer RUNTIME = 139;
    public static final Double POPULARITY = 61.416;
    public static final BigDecimal VOTE_AVERAGE = new BigDecimal("8.433");
    public static final Integer VOTE_COUNT = 26280;
    public static final Instant SYNCED_AT = Instant.parse("2026-08-01T10:00:00Z");

    public static final Long COMEDY_GENRE_ID = 35L;
    public static final Long THRILLER_GENRE_ID = 53L;
    public static final Long HORROR_GENRE_ID = 27L;

    public static final int TMDB_PAGE_SIZE = 20;

    public static final TmdbMovieDetailsResponse tmdbMovieDetailsResponse = tmdbMovieDetailsResponse(MOVIE_ID);
    public static final TmdbMovieSearchResultResponse tmdbMovieSearchResultResponse = tmdbSearchResult(MOVIE_ID, TITLE);
    public static final TmdbSearchResponse tmdbSearchResponse = tmdbSearchResponse(
            1, 10, 200, List.of(tmdbMovieSearchResultResponse)
    );
    public static final PageResponse<MovieSearchResponse> searchPage = searchPage(tmdbSearchResponse);
    public static final Movie movie = movie();
    public static final List<Genre> genres = genres();
    public static final List<MovieTranslation> movieTranslations = movieTranslations(movie);
    public static final MovieSummaryResponse movieSummaryResponse = movieSummaryResponse();


    public static TmdbMovieDetailsResponse tmdbMovieDetailsResponse(Long movieId) {
        return tmdbMovieDetailsResponse(movieId, OVERVIEW, STATUS);
    }

    public static TmdbMovieDetailsResponse tmdbMovieDetailsResponse(Long movieId, String overview, String status) {
        return new TmdbMovieDetailsResponse(
                movieId,
                false,
                false,
                BACKDROP_PATH,
                POSTER_PATH,
                TITLE,
                ORIGINAL_TITLE,
                ORIGINAL_LANGUAGE,
                overview,
                TAGLINE,
                status,
                IMDB_ID,
                RELEASE_DATE,
                RUNTIME,
                POPULARITY,
                VOTE_AVERAGE,
                VOTE_COUNT,
                null,
                tmdbGenreResponseList(),
                tmdbTranslationsResponse()
        );
    }

    public static MovieSearchResponse movieSearchResponse() {
        return new MovieSearchResponse(
                MOVIE_ID,
                TITLE,
                POSTER_PATH,
                RELEASE_DATE,
                VOTE_AVERAGE,
                false
        );
    }

    public static TmdbSearchResponse tmdbSearchResponse(
            int page, int totalPages, int totalResults, List<TmdbMovieSearchResultResponse> tmdbMovieSearchResultResponseList
    ) {
        return new TmdbSearchResponse(
                page,
                tmdbMovieSearchResultResponseList,
                totalPages,
                totalResults
        );
    }

    public static TmdbMovieSearchResultResponse tmdbSearchResult(Long id, String title) {
        return new TmdbMovieSearchResultResponse(
                id,
                false,
                false,
                BACKDROP_PATH,
                POSTER_PATH,
                title,
                title,
                ORIGINAL_LANGUAGE,
                OVERVIEW,
                RELEASE_DATE,
                POPULARITY,
                VOTE_AVERAGE,
                VOTE_COUNT,
                List.of(THRILLER_GENRE_ID, HORROR_GENRE_ID)
        );
    }

    private static PageResponse<MovieSearchResponse> searchPage(TmdbSearchResponse searchResponse) {
        return new PageResponse<>(
                List.of(movieSearchResponse()),
                searchResponse.page(),
                TMDB_PAGE_SIZE,
                searchResponse.totalResults(),
                searchResponse.totalPages(),
                searchResponse.page() >= searchResponse.totalPages()
        );
    }

    private static List<TmdbGenreResponse> tmdbGenreResponseList() {
        return List.of(
                new TmdbGenreResponse(COMEDY_GENRE_ID, "Comedy"),
                new TmdbGenreResponse(THRILLER_GENRE_ID, "Thriller"),
                new TmdbGenreResponse(HORROR_GENRE_ID, "Horror")
        );
    }

    private static TmdbTranslationsResponse tmdbTranslationsResponse() {
        return new TmdbTranslationsResponse(List.of(
                new TmdbTranslationItemResponse(
                        "PL",
                        "pl",
                        "Polski",
                        "Polish",
                        new TmdbTranslationDataResponse(
                                "Podziemny krąg",
                                "Urzędnik cierpiący na bezsenność i sprzedawca mydła zakładają podziemny klub walki.",
                                null,
                                "Psoty. Chaos. Mydło.",
                                RUNTIME
                        )
                ),
                new TmdbTranslationItemResponse(
                        "US",
                        "en",
                        "English",
                        "English",
                        new TmdbTranslationDataResponse(
                                TITLE,
                                OVERVIEW,
                                null,
                                TAGLINE,
                                RUNTIME
                        )
                )
        ));
    }

    private static MovieSummaryResponse movieSummaryResponse() {
        return new MovieSummaryResponse(
                MOVIE_ID,
                TITLE,
                POSTER_PATH,
                RELEASE_DATE,
                RUNTIME,
                VOTE_AVERAGE
        );
    }

    private static Movie movie() {
        return Movie.builder()
                .id(MOVIE_ID)
                .imdbId(IMDB_ID)
                .title(TITLE)
                .originalTitle(ORIGINAL_TITLE)
                .originalLanguage(ORIGINAL_LANGUAGE)
                .overview(OVERVIEW)
                .tagline(TAGLINE)
                .releaseDate(RELEASE_DATE)
                .runtime(RUNTIME)
                .status(STATUS)
                .adult(false)
                .video(false)
                .popularity(POPULARITY)
                .voteAverage(VOTE_AVERAGE)
                .voteCount(VOTE_COUNT)
                .posterPath(POSTER_PATH)
                .backdropPath(BACKDROP_PATH)
                .origin(MovieOrigin.ON_DEMAND)
                .syncedAt(SYNCED_AT)
                .build();
    }

    private static List<Genre> genres() {
        return List.of(
                new Genre(COMEDY_GENRE_ID, "Comedy"),
                new Genre(THRILLER_GENRE_ID, "Thriller"),
                new Genre(HORROR_GENRE_ID, "Horror")
        );
    }

    private static List<MovieTranslation> movieTranslations(Movie movie) {
        return List.of(
                new MovieTranslation(
                        movie,
                        "pl",
                        "Podziemny krąg",
                        "Urzędnik cierpiący na bezsenność i sprzedawca mydła zakładają podziemny klub walki.",
                        "Psoty. Chaos. Mydło."
                )
        );
    }
}
