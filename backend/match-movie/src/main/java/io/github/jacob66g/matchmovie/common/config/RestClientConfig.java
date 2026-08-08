package io.github.jacob66g.matchmovie.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jacob66g.matchmovie.movies.client.TmdbProperties;
import io.github.jacob66g.matchmovie.movies.client.jackson.EmptyStringAsNullLocalDateDeserializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Configuration
@EnableConfigurationProperties(TmdbProperties.class)
public class RestClientConfig {

    @Bean
    RestClient tmdbRestClient(TmdbProperties tmdbProperties) {
        ObjectMapper mapper = tmdbObjectMapper();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));

        return RestClient.builder()
                .baseUrl(tmdbProperties.baseUrl())
                .defaultHeader("Authorization", tmdbProperties.authorizationHeader())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .messageConverters(converters -> {
                    converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(converter);
                })
                .build();
    }

    private static ObjectMapper tmdbObjectMapper() {
        SimpleModule localDateModule = new SimpleModule();
        localDateModule.addDeserializer(LocalDate.class, new EmptyStringAsNullLocalDateDeserializer());

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(localDateModule)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
