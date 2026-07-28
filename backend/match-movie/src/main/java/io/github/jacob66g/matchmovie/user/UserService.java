package io.github.jacob66g.matchmovie.user;

import io.github.jacob66g.matchmovie.common.exception.InvalidTokenException;
import io.github.jacob66g.matchmovie.common.log.ApplicationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void syncUserFromToken(Jwt jwt, String preferredLocate) {
        UUID userId = extractAndValidateUserId(jwt.getSubject());

        //TODO cache user ids
        if (!userRepository.existsById(userId)) {
            createNewUser(userId, jwt, preferredLocate);
        }
    }

    private void createNewUser(UUID userId, Jwt jwt, String preferredLocate) {
        String email = getRequiredClaim(jwt, "email");
        String username = getRequiredClaim(jwt, "preferred_username");

        User user = User.builder()
                .id(userId)
                .email(email)
                .username(username)
                .preferredLocale(preferredLocate)
                .build();

        userRepository.saveAndFlush(user);
        log.info("New user provisioned: userId={} email={}", userId, email);
    }

    private UUID extractAndValidateUserId(String sub) {
        if (!StringUtils.hasText(sub)) {
            throw new InvalidTokenException(
                    new ApplicationLog(Level.WARN, "Missing required JWT claim 'sub'"),
                    "error.authentication.missing.claim", "sub"
            );
        }
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException(
                    new ApplicationLog(Level.ERROR, "Invalid UUID format in JWT claim 'sub': {}", sub),
                    "error.authentication.invalid.sub.format", sub
            );
        }
    }

    private String getRequiredClaim(Jwt jwt, String claimName) {
        String value = jwt.getClaimAsString(claimName);
        if (!StringUtils.hasText(value)) {
            throw new InvalidTokenException(
                    new ApplicationLog(Level.WARN, "Cannot provision user {}: missing required JWT claim {}", jwt.getSubject(), claimName),
                    "error.authentication.missing.claim", claimName
            );
        }
        return value;
    }

}
