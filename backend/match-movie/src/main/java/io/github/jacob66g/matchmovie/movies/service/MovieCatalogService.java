package io.github.jacob66g.matchmovie.movies.service;

import io.github.jacob66g.matchmovie.movies.client.dto.TmdbMovieDetailsResponse;
import io.github.jacob66g.matchmovie.movies.client.mapper.TmdbCatalogMapper;
import io.github.jacob66g.matchmovie.movies.model.*;
import io.github.jacob66g.matchmovie.movies.repository.GenreRepository;
import io.github.jacob66g.matchmovie.movies.repository.MovieRepository;
import io.github.jacob66g.matchmovie.movies.repository.MovieTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class MovieCatalogService {

    private final TmdbCatalogMapper tmdbCatalogMapper;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieTranslationRepository movieTranslationRepository;

    @Transactional(readOnly = true)
    public Optional<Movie> findById(Long tmdbMovieId) {
        return movieRepository.findById(tmdbMovieId);
    }

    @Transactional
    public Movie persistImported(TmdbMovieDetailsResponse details, MovieOrigin origin) {
        return movieRepository.findById(details.id())
                .orElseGet(() -> insertNewMovie(details, origin));
    }

    @Transactional(readOnly = true)
    public Set<Long> findCatalogIds(Set<Long> tmdbMovieIds) {
        if (tmdbMovieIds.isEmpty()) {
            return Set.of();
        }

        return movieRepository.findExistingIds(tmdbMovieIds);
    }

    private Movie insertNewMovie(TmdbMovieDetailsResponse details, MovieOrigin origin) {
        Movie movie = tmdbCatalogMapper.toMovie(details, origin);
        movie.setGenres(resolveGenres(tmdbCatalogMapper.toGenres(details.genres())));
        movieRepository.save(movie);

        List<MovieTranslation> translations = tmdbCatalogMapper.toTranslations(details.translations(), movie);
        if (!translations.isEmpty()) {
            movieTranslationRepository.saveAll(translations);
        }

        return movie;
    }

    private Set<Genre> resolveGenres(List<Genre> genres) {
        if (genres.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<Long> ids = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Genre> existingGenres = genreRepository.findAllById(ids);
        if (existingGenres.size() == ids.size()) {
            return new LinkedHashSet<>(existingGenres);
        }

        List<Genre> missingGenres = genres.stream()
                .filter(genre -> !existingGenres.contains(genre))
                .sorted(Comparator.comparing(Genre::getId))
                .toList();

        log.info("Adding genres {} first seen while importing a movie", missingGenres.stream().map(Genre::getId).toList());
        missingGenres.forEach(genre -> genreRepository.insertIfAbsent(genre.getId(), genre.getName()));

        return new LinkedHashSet<>(genreRepository.findAllById(ids));
    }
}
