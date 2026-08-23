package io.github.jacob66g.matchmovie.movies.client.mapper;

import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.client.dto.*;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import io.github.jacob66g.matchmovie.movies.model.Genre;
import io.github.jacob66g.matchmovie.movies.model.Movie;
import io.github.jacob66g.matchmovie.movies.model.MovieOrigin;
import io.github.jacob66g.matchmovie.movies.model.MovieTranslation;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.jacob66g.matchmovie.movies.client.mapper.TmdbUtils.*;


@Component
public class TmdbCatalogMapper {

    public static final Set<String> CATALOGUE_TRANSLATION_LANGUAGES = Set.of("pl");

    public Movie toMovie(TmdbMovieDetailsResponse details, MovieOrigin origin) {
        requirePersistableDetails(details);

        String title = StringUtils.hasText(details.title()) ? details.title() : details.originalTitle();
        String originalTitle = StringUtils.hasText(details.originalTitle()) ? details.originalTitle() : details.title();
        TmdbCollectionResponse collection = details.belongsToCollection();

        return Movie.builder()
                .id(details.id())
                .imdbId(blankToNull(details.imdbId()))
                .title(title.trim())
                .originalTitle(originalTitle.trim())
                .originalLanguage(blankToNull(details.originalLanguage()))
                .overview(blankToNull(details.overview()))
                .tagline(blankToNull(details.tagline()))
                .releaseDate(details.releaseDate())
                .runtime(normalizeRuntime(details.runtime()))
                .status(blankToNull(details.status()))
                .adult(details.adult())
                .video(details.video())
                .popularity(details.popularity())
                .voteAverage(normalizeVoteAverage(details.voteAverage()))
                .voteCount(normalizeVoteCount(details.voteCount()))
                .posterPath(blankToNull(details.posterPath()))
                .backdropPath(blankToNull(details.backdropPath()))
                .collectionId(collection != null ? collection.id() : null)
                .collectionName(collection != null ? blankToNull(collection.name()) : null)
                .origin(origin)
                .build();
    }

    public List<Genre> toGenres(List<TmdbGenreResponse> genreResponse) {
        if (genreResponse == null || genreResponse.isEmpty()) {
            return List.of();
        }

        return genreResponse.stream()
                .filter(genre -> genre != null && genre.id() != null && StringUtils.hasText(genre.name()))
                .map(genre -> new Genre(genre.id(), genre.name().trim()))
                .toList();
    }

    public List<MovieTranslation> toTranslations(TmdbTranslationsResponse translationsResponse, Movie movie) {
        if (translationsResponse == null || translationsResponse.translations() == null || translationsResponse.translations().isEmpty()) {
            return List.of();
        }

        List<MovieTranslation> result = new ArrayList<>();
        for (String language : CATALOGUE_TRANSLATION_LANGUAGES) {
            translationsResponse.translations().stream()
                    .filter(item -> item != null && item.data() != null && matchesLanguage(item, language))
                    .findFirst()
                    .map(item -> toTranslation(movie, language, item.data()))
                    .filter(this::hasAnyTranslationText)
                    .ifPresent(result::add);
        }
        return result;
    }

    private void requirePersistableDetails(TmdbMovieDetailsResponse details) {
        if (details.id() == null) {
            throw new ApplicationException(MovieErrorCode.TMDB_INVALID_RESPONSE)
                    .with("reason", "missing tmdb id");
        }
        if (!StringUtils.hasText(details.title()) && !StringUtils.hasText(details.originalTitle())) {
            throw new ApplicationException(MovieErrorCode.TMDB_INVALID_RESPONSE)
                    .with("movieId", details.id())
                    .with("reason", "missing title");
        }
    }

    private MovieTranslation toTranslation(Movie movie, String languageCode, TmdbTranslationDataResponse data) {
        return new MovieTranslation(
                movie,
                languageCode,
                blankToNull(data.title()),
                blankToNull(data.overview()),
                blankToNull(data.tagline())
        );
    }

    private static boolean matchesLanguage(TmdbTranslationItemResponse item, String language) {
        return language.equalsIgnoreCase(item.iso6391());
    }

    private boolean hasAnyTranslationText(MovieTranslation translation) {
        return StringUtils.hasText(translation.getTitle())
                || StringUtils.hasText(translation.getOverview())
                || StringUtils.hasText(translation.getTagline());
    }
}
