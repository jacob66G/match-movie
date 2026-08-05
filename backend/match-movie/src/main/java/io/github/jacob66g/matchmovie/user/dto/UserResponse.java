package io.github.jacob66g.matchmovie.user.dto;

import io.github.jacob66g.matchmovie.user.model.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String preferredLocate,
        Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPreferredLocale(),
                user.getCreatedAt()
        );
    }
}
