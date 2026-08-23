package io.github.jacob66g.matchmovie.movies.client.mapper;

import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.client.dto.*;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.jacob66g.matchmovie.movies.model.Genre;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import io.github.jacob66g.matchmovie.movies.model.MovieTranslation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static io.github.jacob66g.matchmovie.movies.MovieFactory.*;
import static org.assertj.core.api.Assertions.*;

class TmdbCatalogMapperTest {

    private final TmdbCatalogMapper mapper = new TmdbCatalogMapper();

    @Test
    void should_map_all_fields_from_tmdb_details() {
        //when
        Movie result = mapper.toMovie(tmdbMovieDetailsResponse, MovieOrigin.ON_DEMAND);

        //then
        assertThat(result.getId()).isEqualTo(MOVIE_ID);
        assertThat(result.getImdbId()).isEqualTo(IMDB_ID);
        assertThat(result.getTitle()).isEqualTo(TITLE);
        assertThat(result.getOriginalTitle()).isEqualTo(ORIGINAL_TITLE);
        assertThat(result.getOriginalLanguage()).isEqualTo(ORIGINAL_LANGUAGE);
        assertThat(result.getOverview()).isEqualTo(OVERVIEW);
        assertThat(result.getTagline()).isEqualTo(TAGLINE);
        assertThat(result.getReleaseDate()).isEqualTo(RELEASE_DATE);
        assertThat(result.getRuntime()).isEqualTo(RUNTIME);
        assertThat(result.getStatus()).isEqualTo(STATUS);
        assertThat(result.isAdult()).isFalse();
        assertThat(result.isVideo()).isFalse();
        assertThat(result.getPopularity()).isEqualTo(POPULARITY);
        assertThat(result.getVoteAverage()).isEqualByComparingTo(VOTE_AVERAGE);
        assertThat(result.getVoteCount()).isEqualTo(VOTE_COUNT);
        assertThat(result.getPosterPath()).isEqualTo(POSTER_PATH);
        assertThat(result.getBackdropPath()).isEqualTo(BACKDROP_PATH);
        assertThat(result.getOrigin()).isEqualTo(MovieOrigin.ON_DEMAND);
        assertThat(result.getCollectionId()).isNull();
        assertThat(result.getCollectionName()).isNull();
    }

    @Test
    void should_map_collection_when_present() {
        //given
        TmdbMovieDetailsResponse details = detailsWithCollection(new TmdbCollectionResponse(10L, "  Fight Club Collection  ", null, null));

        //when
        Movie result = mapper.toMovie(details, MovieOrigin.SEED);

        //then
        assertThat(result.getCollectionId()).isEqualTo(10L);
        assertThat(result.getCollectionName()).isEqualTo("Fight Club Collection");
    }

    @Test
    void should_use_original_title_when_title_is_blank() {
        //given
        TmdbMovieDetailsResponse details = detailsWithTitles("  ", ORIGINAL_TITLE);

        //when
        Movie result = mapper.toMovie(details, MovieOrigin.ON_DEMAND);

        //then
        assertThat(result.getTitle()).isEqualTo(ORIGINAL_TITLE);
        assertThat(result.getOriginalTitle()).isEqualTo(ORIGINAL_TITLE);
    }

    @Test
    void should_use_title_when_original_title_is_blank() {
        //given
        TmdbMovieDetailsResponse details = detailsWithTitles(TITLE, "  ");

        //when
        Movie result = mapper.toMovie(details, MovieOrigin.ON_DEMAND);

        //then
        assertThat(result.getTitle()).isEqualTo(TITLE);
        assertThat(result.getOriginalTitle()).isEqualTo(TITLE);
    }

    @Test
    void should_trim_title_and_original_title() {
        //given
        TmdbMovieDetailsResponse details = detailsWithTitles("  " + TITLE + "  ", "  " + ORIGINAL_TITLE + "  ");

        //when
        Movie result = mapper.toMovie(details, MovieOrigin.ON_DEMAND);

        //then
        assertThat(result.getTitle()).isEqualTo(TITLE);
        assertThat(result.getOriginalTitle()).isEqualTo(ORIGINAL_TITLE);
    }

    @Test
    void should_convert_blank_optional_fields_to_null() {
        //given
        TmdbMovieDetailsResponse details = new TmdbMovieDetailsResponse(
                MOVIE_ID, false, false, "   ", "   ",
                TITLE, ORIGINAL_TITLE, "   ", "   ", "   ", "   ", "   ",
                RELEASE_DATE, RUNTIME, POPULARITY, VOTE_AVERAGE, VOTE_COUNT,
                null, List.of(), null
        );

        //when
        Movie result = mapper.toMovie(details, MovieOrigin.ON_DEMAND);

        //then
        assertThat(result.getImdbId()).isNull();
        assertThat(result.getOriginalLanguage()).isNull();
        assertThat(result.getOverview()).isNull();
        assertThat(result.getTagline()).isNull();
        assertThat(result.getStatus()).isNull();
        assertThat(result.getPosterPath()).isNull();
        assertThat(result.getBackdropPath()).isNull();
    }

    @Test
    void should_normalize_non_positive_runtime_to_null() {
        //given
        TmdbMovieDetailsResponse zeroRuntime = detailsWithRuntime(0);
        TmdbMovieDetailsResponse negativeRuntime = detailsWithRuntime(-10);

        //when + then
        assertThat(mapper.toMovie(zeroRuntime, MovieOrigin.ON_DEMAND).getRuntime()).isNull();
        assertThat(mapper.toMovie(negativeRuntime, MovieOrigin.ON_DEMAND).getRuntime()).isNull();
    }

    @Test
    void should_normalize_out_of_range_vote_average_to_null() {
        //given
        TmdbMovieDetailsResponse tooHigh = detailsWithVoteAverage(new BigDecimal("10.5"));
        TmdbMovieDetailsResponse negative = detailsWithVoteAverage(new BigDecimal("-1"));

        //when + then
        assertThat(mapper.toMovie(tooHigh, MovieOrigin.ON_DEMAND).getVoteAverage()).isNull();
        assertThat(mapper.toMovie(negative, MovieOrigin.ON_DEMAND).getVoteAverage()).isNull();
    }

    @Test
    void should_normalize_negative_vote_count_to_zero() {
        //given
        TmdbMovieDetailsResponse details = detailsWithVoteCount(-5);

        //when
        Movie result = mapper.toMovie(details, MovieOrigin.ON_DEMAND);

        //then
        assertThat(result.getVoteCount()).isZero();
    }

    @Test
    void should_throw_ApplicationException_when_id_is_missing() {
        //given
        TmdbMovieDetailsResponse details = new TmdbMovieDetailsResponse(
                null, false, false, BACKDROP_PATH, POSTER_PATH,
                TITLE, ORIGINAL_TITLE, ORIGINAL_LANGUAGE, OVERVIEW, TAGLINE, STATUS, IMDB_ID,
                RELEASE_DATE, RUNTIME, POPULARITY, VOTE_AVERAGE, VOTE_COUNT,
                null, List.of(), null
        );

        //when + then
        assertThatThrownBy(() -> mapper.toMovie(details, MovieOrigin.ON_DEMAND))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(MovieErrorCode.TMDB_INVALID_RESPONSE);
    }

    @Test
    void should_throw_ApplicationException_when_title_and_original_title_are_both_blank() {
        //given
        TmdbMovieDetailsResponse details = detailsWithTitles("  ", "  ");

        //when + then
        assertThatThrownBy(() -> mapper.toMovie(details, MovieOrigin.ON_DEMAND))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(MovieErrorCode.TMDB_INVALID_RESPONSE);
    }

    @Test
    void should_map_genres_and_trim_names() {
        //given
        List<TmdbGenreResponse> genreResponses = List.of(
                new TmdbGenreResponse(COMEDY_GENRE_ID, "  Comedy  "),
                new TmdbGenreResponse(THRILLER_GENRE_ID, "Thriller")
        );

        //when
        List<Genre> result = mapper.toGenres(genreResponses);

        //then
        assertThat(result)
                .extracting(Genre::getId, Genre::getName)
                .containsExactly(
                        tuple(COMEDY_GENRE_ID, "Comedy"),
                        tuple(THRILLER_GENRE_ID, "Thriller")
                );
    }

    @Test
    void should_return_empty_list_when_genre_response_is_null() {
        //when + then
        assertThat(mapper.toGenres(null)).isEmpty();
    }

    @Test
    void should_return_empty_list_when_genre_response_is_empty() {
        //when + then
        assertThat(mapper.toGenres(List.of())).isEmpty();
    }

    @Test
    void should_filter_out_genres_with_missing_id_or_blank_name() {
        //given
        List<TmdbGenreResponse> genreResponses = new java.util.ArrayList<>(List.of(
                new TmdbGenreResponse(null, "Missing id"),
                new TmdbGenreResponse(COMEDY_GENRE_ID, "  "),
                new TmdbGenreResponse(THRILLER_GENRE_ID, "Thriller")
        ));
        genreResponses.add(null);

        //when
        List<Genre> result = mapper.toGenres(genreResponses);

        //then
        assertThat(result)
                .extracting(Genre::getId)
                .containsExactly(THRILLER_GENRE_ID);
    }

    @Test
    void should_map_the_polish_translation_when_present() {
        //when
        List<MovieTranslation> result = mapper.toTranslations(tmdbMovieDetailsResponse.translations(), movie);

        //then
        assertThat(result)
                .singleElement()
                .satisfies(translation -> {
                    assertThat(translation.getId().getLanguageCode()).isEqualTo("pl");
                    assertThat(translation.getTitle()).isEqualTo("Podziemny krąg");
                });
    }

    @Test
    void should_return_empty_list_when_translations_response_is_null() {
        //when + then
        assertThat(mapper.toTranslations(null, movie)).isEmpty();
    }

    @Test
    void should_return_empty_list_when_translations_list_is_null() {
        //given
        TmdbTranslationsResponse response = new TmdbTranslationsResponse(null);

        //when + then
        assertThat(mapper.toTranslations(response, movie)).isEmpty();
    }

    @Test
    void should_return_empty_list_when_translations_list_is_empty() {
        //given
        TmdbTranslationsResponse response = new TmdbTranslationsResponse(List.of());

        //when + then
        assertThat(mapper.toTranslations(response, movie)).isEmpty();
    }

    @Test
    void should_return_empty_list_when_no_polish_translation_is_present() {
        //given
        TmdbTranslationsResponse response = new TmdbTranslationsResponse(List.of(
                translationItem("en", new TmdbTranslationDataResponse(TITLE, OVERVIEW, null, TAGLINE, RUNTIME))
        ));

        //when + then
        assertThat(mapper.toTranslations(response, movie)).isEmpty();
    }

    @Test
    void should_skip_translation_with_no_readable_text() {
        //given
        TmdbTranslationsResponse response = new TmdbTranslationsResponse(List.of(
                translationItem("pl", new TmdbTranslationDataResponse("  ", "  ", null, "  ", RUNTIME))
        ));

        //when + then
        assertThat(mapper.toTranslations(response, movie)).isEmpty();
    }

    @Test
    void should_match_language_case_insensitively_and_trim_text_fields() {
        //given
        TmdbTranslationsResponse response = new TmdbTranslationsResponse(List.of(
                translationItem("PL", new TmdbTranslationDataResponse("  Tytuł  ", "  Opis  ", null, "  Hasło  ", RUNTIME))
        ));

        //when
        List<MovieTranslation> result = mapper.toTranslations(response, movie);

        //then
        assertThat(result)
                .singleElement()
                .satisfies(translation -> {
                    assertThat(translation.getTitle()).isEqualTo("Tytuł");
                    assertThat(translation.getOverview()).isEqualTo("Opis");
                    assertThat(translation.getTagline()).isEqualTo("Hasło");
                });
    }

    @Test
    void should_ignore_translation_items_without_data() {
        //given
        TmdbTranslationsResponse response = new TmdbTranslationsResponse(java.util.Arrays.asList(
                new TmdbTranslationItemResponse("PL", "pl", "Polski", "Polish", null),
                (TmdbTranslationItemResponse) null
        ));

        //when + then
        assertThat(mapper.toTranslations(response, movie)).isEmpty();
    }

    private TmdbTranslationItemResponse translationItem(String languageCode, TmdbTranslationDataResponse data) {
        return new TmdbTranslationItemResponse(languageCode.toUpperCase(), languageCode, "name", "englishName", data);
    }

    private TmdbMovieDetailsResponse detailsWithCollection(TmdbCollectionResponse collection) {
        return new TmdbMovieDetailsResponse(
                MOVIE_ID, false, false, BACKDROP_PATH, POSTER_PATH,
                TITLE, ORIGINAL_TITLE, ORIGINAL_LANGUAGE, OVERVIEW, TAGLINE, STATUS, IMDB_ID,
                RELEASE_DATE, RUNTIME, POPULARITY, VOTE_AVERAGE, VOTE_COUNT,
                collection, List.of(), null
        );
    }

    private TmdbMovieDetailsResponse detailsWithTitles(String title, String originalTitle) {
        return new TmdbMovieDetailsResponse(
                MOVIE_ID, false, false, BACKDROP_PATH, POSTER_PATH,
                title, originalTitle, ORIGINAL_LANGUAGE, OVERVIEW, TAGLINE, STATUS, IMDB_ID,
                RELEASE_DATE, RUNTIME, POPULARITY, VOTE_AVERAGE, VOTE_COUNT,
                null, List.of(), null
        );
    }

    private TmdbMovieDetailsResponse detailsWithRuntime(Integer runtime) {
        return new TmdbMovieDetailsResponse(
                MOVIE_ID, false, false, BACKDROP_PATH, POSTER_PATH,
                TITLE, ORIGINAL_TITLE, ORIGINAL_LANGUAGE, OVERVIEW, TAGLINE, STATUS, IMDB_ID,
                RELEASE_DATE, runtime, POPULARITY, VOTE_AVERAGE, VOTE_COUNT,
                null, List.of(), null
        );
    }

    private TmdbMovieDetailsResponse detailsWithVoteAverage(BigDecimal voteAverage) {
        return new TmdbMovieDetailsResponse(
                MOVIE_ID, false, false, BACKDROP_PATH, POSTER_PATH,
                TITLE, ORIGINAL_TITLE, ORIGINAL_LANGUAGE, OVERVIEW, TAGLINE, STATUS, IMDB_ID,
                RELEASE_DATE, RUNTIME, POPULARITY, voteAverage, VOTE_COUNT,
                null, List.of(), null
        );
    }

    private TmdbMovieDetailsResponse detailsWithVoteCount(Integer voteCount) {
        return new TmdbMovieDetailsResponse(
                MOVIE_ID, false, false, BACKDROP_PATH, POSTER_PATH,
                TITLE, ORIGINAL_TITLE, ORIGINAL_LANGUAGE, OVERVIEW, TAGLINE, STATUS, IMDB_ID,
                RELEASE_DATE, RUNTIME, POPULARITY, VOTE_AVERAGE, voteCount,
                null, List.of(), null
        );
    }
}
