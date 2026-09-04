package io.github.jacob66g.matchmovie.movies.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.movies.client.dto.*;
import io.github.jacob66g.matchmovie.movies.client.jackson.EmptyStringAsNullLocalDateDeserializer;
import io.github.jacob66g.matchmovie.movies.exception.MovieErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TmdbClientTest {

    private static final String BASE_URL = "http://localhost";
    private static final String TOKEN = "test-token";

    private MockRestServiceServer server;
    private TmdbClient tmdbClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        ObjectMapper mapper = tmdbObjectMapper();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);

        RestClient restClient = builder
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Bearer " + TOKEN)
                .messageConverters(converters -> {
                    converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(converter);
                })
                .build();

        TmdbProperties properties = new TmdbProperties(
                BASE_URL,
                "3",
                "en-US",
                "https://image.tmdb.org/t/p/",
                new TmdbProperties.Auth(TOKEN)
        );

        tmdbClient = new TmdbClient(restClient, properties);
    }

    @Test
    void should_fetch_movie_details_and_map_snake_case_fields() {
        server.expect(requestTo(BASE_URL + "/movie/11?language=en-US&append_to_response=translations"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + TOKEN))
                .andRespond(withSuccess("""
                        {
                          "id": 11,
                          "adult": false,
                          "video": false,
                          "backdrop_path": "/backdrop.jpg",
                          "poster_path": "/poster.jpg",
                          "title": "Star Wars",
                          "original_title": "Star Wars",
                          "original_language": "en",
                          "overview": "A long time ago...",
                          "tagline": "A long time ago in a galaxy far, far away...",
                          "status": "Released",
                          "imdb_id": "tt0076759",
                          "release_date": "1977-05-25",
                          "runtime": 121,
                          "popularity": 20.69,
                          "vote_average": 8.2,
                          "vote_count": 22061,
                          "belongs_to_collection": {
                            "id": 10,
                            "name": "Star Wars Collection",
                            "poster_path": "/collection-poster.jpg",
                            "backdrop_path": "/collection-backdrop.jpg"
                          },
                          "genres": [
                            { "id": 12, "name": "Adventure" },
                            { "id": 28, "name": "Action" }
                          ],
                          "translations": {
                            "translations": [
                              {
                                "iso_3166_1": "PL",
                                "iso_639_1": "pl",
                                "name": "Polski",
                                "english_name": "Polish",
                                "data": {
                                  "title": "Gwiezdne wojny",
                                  "overview": "Polski opis",
                                  "tagline": "Dawno, dawno temu..."
                                }
                              }
                            ]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        TmdbMovieDetailsResponse details = tmdbClient.getMovieDetails(11, false);

        assertThat(details.id()).isEqualTo(11L);
        assertThat(details.title()).isEqualTo("Star Wars");
        assertThat(details.releaseDate()).isEqualTo(LocalDate.of(1977, 5, 25));
        assertThat(details.voteAverage()).isEqualByComparingTo(new BigDecimal("8.2"));
        assertThat(details.belongsToCollection().name()).isEqualTo("Star Wars Collection");
        assertThat(details.genres()).extracting(TmdbGenreResponse::id).containsExactly(12L, 28L);
        assertThat(details.translations().translations())
                .singleElement()
                .satisfies(translation -> {
                    assertThat(translation.iso6391()).isEqualTo("pl");
                    assertThat(translation.data().title()).isEqualTo("Gwiezdne wojny");
                });
        server.verify();
    }

    @Test
    void should_map_empty_release_date_to_null() {
        server.expect(requestTo(BASE_URL + "/movie/1?language=en-US&append_to_response=translations"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "id": 1,
                          "adult": false,
                          "video": false,
                          "title": "Untitled",
                          "release_date": "",
                          "vote_count": 0,
                          "genres": [],
                          "translations": { "translations": [] }
                        }
                        """, MediaType.APPLICATION_JSON));

        TmdbMovieDetailsResponse details = tmdbClient.getMovieDetails(1, false);

        assertThat(details.releaseDate()).isNull();
        server.verify();
    }

    @Test
    void should_throw_ApplicationException_when_movie_is_missing_in_tmdb() {
        server.expect(requestTo(BASE_URL + "/movie/999?language=en-US&append_to_response=translations"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> tmdbClient.getMovieDetails(999, false))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(MovieErrorCode.TMDB_MOVIE_NOT_FOUND);
        server.verify();
    }

    @Test
    void should_throw_ApplicationException_when_tmdb_answers_without_a_body() {
        server.expect(requestTo(BASE_URL + "/search/movie?query=Fight%20Club&page=1&include_adult=false&language=en-US"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertThatThrownBy(() -> tmdbClient.searchMovies("Fight Club", 1))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(MovieErrorCode.TMDB_INVALID_RESPONSE);
        server.verify();
    }

    @Test
    void should_search_movies() {
        server.expect(requestTo(BASE_URL + "/search/movie?query=Fight%20Club&page=1&include_adult=false&language=en-US"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "page": 1,
                          "results": [
                            {
                              "id": 550,
                              "adult": false,
                              "video": false,
                              "title": "Fight Club",
                              "original_title": "Fight Club",
                              "original_language": "en",
                              "overview": "An insomnia...",
                              "poster_path": "/poster.jpg",
                              "backdrop_path": "/backdrop.jpg",
                              "release_date": "1999-10-15",
                              "popularity": 73.4,
                              "vote_average": 8.433,
                              "vote_count": 26279,
                              "genre_ids": [18, 53, 35]
                            }
                          ],
                          "total_pages": 1,
                          "total_results": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        TmdbSearchResponse response = tmdbClient.searchMovies("Fight Club", 1);

        assertThat(response.totalResults()).isEqualTo(1);
        assertThat(response.results())
                .singleElement()
                .satisfies(result -> {
                    assertThat(result.id()).isEqualTo(550L);
                    assertThat(result.title()).isEqualTo("Fight Club");
                    assertThat(result.genreIds()).containsExactly(18L, 53L, 35L);
                    assertThat(result.voteAverage()).isEqualByComparingTo(new BigDecimal("8.433"));
                });
        server.verify();
    }

    @Test
    void should_list_genres() {
        server.expect(requestTo(BASE_URL + "/genre/movie/list?language=en-US"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "genres": [
                            { "id": 28, "name": "Action" },
                            { "id": 12, "name": "Adventure" }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        TmdbGenreListResponse response = tmdbClient.listGenres();

        assertThat(response.genres()).extracting(TmdbGenreResponse::name)
                .containsExactly("Action", "Adventure");
        server.verify();
    }

    @Test
    void should_discover_movies_and_map_parameters_correctly() {
        TmdbDiscoverQuery query = new TmdbDiscoverQuery(
                LocalDate.of(2022, 12, 1),
                LocalDate.of(2023, 12, 1),
                1000,
                TmdbSortField.VOTE_AVERAGE_DESC
        );

        String expectedUrl = BASE_URL + "/discover/movie" +
                "?primary_release_date.gte=2022-12-01" +
                "&primary_release_date.lte=2023-12-01" +
                "&vote_count.gte=1000" +
                "&sort_by=vote_average.desc" +
                "&page=1" +
                "&include_adult=false" +
                "&language=en-US";

        server.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                             "page": 1,
                             "results": [
                               {
                                 "adult": false,
                                 "backdrop_path": "/kJsPVzdyBrYHLomuNv5SJDXUQ2f.jpg",
                                 "genre_ids": [
                                   28,
                                   12,
                                   878
                                 ],
                                 "id": 76600,
                                 "title": "Avatar: The Way of Water",
                                 "original_language": "en",
                                 "original_title": "Avatar: The Way of Water",
                                 "overview": "Set more than a decade after the events of the first film, learn the story of the Sully family (Jake, Neytiri, and their kids), the trouble that follows them, the lengths they go to keep each other safe, the battles they fight to stay alive, and the tragedies they endure.",
                                 "popularity": 39.193,
                                 "poster_path": "/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg",
                                 "release_date": "2022-12-14",
                                 "softcore": false,
                                 "video": false,
                                 "vote_average": 7.588,
                                 "vote_count": 14544
                               },
                               {
                                 "adult": false,
                                 "backdrop_path": "/neeNHeXjMF5fXoCJRsOmkNGC7q.jpg",
                                 "genre_ids": [
                                   18,
                                   36
                                 ],
                                 "id": 872585,
                                 "title": "Oppenheimer",
                                 "original_language": "en",
                                 "original_title": "Oppenheimer",
                                 "overview": "The story of J. Robert Oppenheimer's role in the development of the atomic bomb during World War II.",
                                 "popularity": 44.0394,
                                 "poster_path": "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                                 "release_date": "2023-07-19",
                                 "softcore": false,
                                 "video": false,
                                 "vote_average": 8.022,
                                 "vote_count": 12316
                               }
                            ],
                           "total_pages": 6,
                           "total_results": 114
                         }
                        """, MediaType.APPLICATION_JSON));

        TmdbSearchResponse response = tmdbClient.discoverMovies(query, 1);


        assertThat(response.totalPages()).isEqualTo(6);
        assertThat(response.totalResults()).isEqualTo(114);
        assertThat(response.results()).hasSize(2);

        assertThat(response.results()).element(0).satisfies(result -> {
            assertThat(result.id()).isEqualTo(76600L);
            assertThat(result.title()).isEqualTo("Avatar: The Way of Water");
            assertThat(result.genreIds()).containsExactly(28L, 12L, 878L);
            assertThat(result.voteAverage()).isEqualByComparingTo(new BigDecimal("7.588"));
        });

        assertThat(response.results()).element(1).satisfies(result -> {
            assertThat(result.id()).isEqualTo(872585);
            assertThat(result.title()).isEqualTo("Oppenheimer");
            assertThat(result.genreIds()).containsExactly(18L, 36L);
            assertThat(result.voteAverage()).isEqualByComparingTo(new BigDecimal("8.022"));
        });

        server.verify();
    }

    @Test
    void should_prefix_bearer_when_token_has_no_scheme() {
        assertThat(new TmdbProperties(
                BASE_URL, "3", "en-US", "https://image.tmdb.org/t/p/", new TmdbProperties.Auth("raw-token")
        ).authorizationHeader()).isEqualTo("Bearer raw-token");
    }

    @Test
    void should_keep_existing_bearer_prefix() {
        assertThat(new TmdbProperties(
                BASE_URL, "3", "en-US", "https://image.tmdb.org/t/p/", new TmdbProperties.Auth("Bearer already")
        ).authorizationHeader()).isEqualTo("Bearer already");
    }

    private static ObjectMapper tmdbObjectMapper() {
        SimpleModule localDateModule = new SimpleModule();
        localDateModule.addDeserializer(java.time.LocalDate.class, new EmptyStringAsNullLocalDateDeserializer());

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(localDateModule)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
