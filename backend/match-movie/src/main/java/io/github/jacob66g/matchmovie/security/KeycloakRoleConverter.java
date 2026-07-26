package io.github.jacob66g.matchmovie.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private String clientId;

    public KeycloakRoleConverter(String clientId) {
        this.clientId = clientId;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> resourceAccess = (Map<String, Object>) jwt.getClaims().get("resource_access");

        return Optional.ofNullable(resourceAccess)
                .map(access -> (Map<String, Object>) access.get(clientId))
                .map(client -> (List<String>) client.get("roles"))
                .orElse(Collections.emptyList())
                .stream()
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toList());

    }
}
