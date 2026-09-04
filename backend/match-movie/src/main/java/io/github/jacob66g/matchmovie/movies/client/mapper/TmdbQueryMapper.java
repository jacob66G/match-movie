package io.github.jacob66g.matchmovie.movies.client.mapper;

import io.github.jacob66g.matchmovie.movies.client.dto.TmdbDiscoverQuery;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class TmdbQueryMapper {

    private TmdbQueryMapper() {}

    public static MultiValueMap<String, String> toQueryParams (TmdbDiscoverQuery query) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        if (query.releaseDateFrom() != null) {
            params.add("primary_release_date.gte", query.releaseDateFrom().toString());
        }
        if (query.releaseDateTo() != null) {
            params.add("primary_release_date.lte", query.releaseDateTo().toString());
        }
        if (query.minVoteCount() != null) {
            params.add("vote_count.gte", String.valueOf(query.minVoteCount()));
        }
        if (query.sortBy() != null) {
            params.add("sort_by", query.sortBy().getApiValue());
        }

        return params;
    }
}
