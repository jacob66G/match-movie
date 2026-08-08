package io.github.jacob66g.matchmovie.user.service;

import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.user.repository.UserRepository;
import io.github.jacob66g.matchmovie.user.dto.UserResponse;
import io.github.jacob66g.matchmovie.user.exception.UserErrorCode;
import io.github.jacob66g.matchmovie.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional(readOnly = true)
    public UserResponse getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCode.USER_NOT_FOUND)
                        .with("userId", userId));

        return UserResponse.from(user);
    }

    @Transactional
    public UUID syncUserFromToken(Jwt jwt, String preferredLocale) {
        UUID userId = extractAndValidateUserId(jwt.getSubject());

        if (!userRepository.existsById(userId)) {
            createNewUser(userId, jwt, preferredLocale);
        }
        return userId;
    }

    private void createNewUser(UUID userId, Jwt jwt, String preferredLocale) {
        String email = getRequiredClaim(jwt, "email");
        String username = getRequiredClaim(jwt, "preferred_username");

        User user = User.builder()
                .id(userId)
                .email(email)
                .username(username)
                .preferredLocale(preferredLocale)
                .build();

        userRepository.saveAndFlush(user);
        log.info("New user provisioned: userId={}", userId);
    }

    private UUID extractAndValidateUserId(String sub) {
        if (!StringUtils.hasText(sub)) {
            throw new ApplicationException(UserErrorCode.MISSING_SUB_CLAIM, "sub");
        }
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException ex) {
            throw new ApplicationException(UserErrorCode.INVALID_SUB_FORMAT)
                    .causedBy(ex)
                    .with("sub", sub);
        }
    }

    private String getRequiredClaim(Jwt jwt, String claimName) {
        String value = jwt.getClaimAsString(claimName);
        if (!StringUtils.hasText(value)) {
            throw new ApplicationException(UserErrorCode.MISSING_CLAIM_ON_PROVISION, claimName)
                    .with("claim", claimName);
        }
        return value;
    }

}
