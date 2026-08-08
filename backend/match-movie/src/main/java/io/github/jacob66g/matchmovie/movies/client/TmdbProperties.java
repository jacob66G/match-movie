package io.github.jacob66g.matchmovie.movies.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "client.tmdb")
public record TmdbProperties(
        String api,
        String version,
        String language,
        String imageBaseUrl,
        Auth auth
) {

    public record Auth(String token) {
    }

    public String baseUrl() {
        return api + "/" + version;
    }

    public String authorizationHeader() {
        String token = auth.token();
        if (!StringUtils.hasText(token)) {
            return token;
        }

        return token.startsWith("Bearer ") ? token : "Bearer " +  token;
    }
}
