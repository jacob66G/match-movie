package io.github.jacob66g.matchmovie.user;

import io.github.jacob66g.matchmovie.common.exception.ApplicationException;
import io.github.jacob66g.matchmovie.user.exception.UserErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private static final UUID TEST_USER_ID = UUID.fromString("6e9b93f1-62f0-41e9-9341-59a215bbcc46");
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testName";
    private static final String TEST_LOCALE = "en";
    private static final Map<String, Object> TEST_HEADERS = Map.of("typ", "JWT");

    private Map<String, Object> testClaims;

    @BeforeEach
    void setup() {
        testClaims = new HashMap<>();
        testClaims.put("email", TEST_EMAIL);
        testClaims.put("preferred_username", TEST_USERNAME);
        testClaims.put("sub", TEST_USER_ID);
    }


    @Test
    void should_not_create_user_when_user_already_exists() {
        //given
        Jwt jwt = buildJwt(testClaims);
        when(userRepository.existsById(TEST_USER_ID)).thenReturn(true);

        //when
        userService.syncUserFromToken(jwt, TEST_LOCALE);

        //then
        verify(userRepository, never()).saveAndFlush(any());
    }


    @Test
    void should_throw_ApplicationException_when_sub_claim_is_missing() {
        //given
        testClaims.remove("sub");
        Jwt jwt = buildJwt(testClaims);

        //when + then
        assertThatThrownBy(() -> userService.syncUserFromToken(jwt, TEST_LOCALE))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.MISSING_SUB_CLAIM);
        verify(userRepository, never()).existsById(any());
    }

    @Test
    void should_throw_ApplicationException_when_sub_claim_is_not_valid_uuid() {
        //given
        testClaims.put("sub", "INVALID_UUID_FORMAT");
        Jwt jwt = buildJwt(testClaims);

        //when + then
        assertThatThrownBy(() -> userService.syncUserFromToken(jwt, TEST_LOCALE))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.INVALID_SUB_FORMAT);
        verify(userRepository, never()).existsById(any());
    }

    @Test
    void should_create_new_user_when_user_does_not_exist() {
        //given
        Jwt jwt = buildJwt(testClaims);
        when(userRepository.existsById(TEST_USER_ID)).thenReturn(false);

        //when
        userService.syncUserFromToken(jwt, TEST_LOCALE);

        //then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).saveAndFlush(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getId()).isEqualTo(TEST_USER_ID);
        assertThat(savedUser.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(savedUser.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(savedUser.getPreferredLocale()).isEqualTo(TEST_LOCALE);
    }

    @Test
    void should_throw_ApplicationException_when_required_claim_is_missing() {
        //given
        testClaims.remove("email");
        Jwt jwt = buildJwt(testClaims);
        when(userRepository.existsById(TEST_USER_ID)).thenReturn(false);

        //when + then
        assertThatThrownBy(() -> userService.syncUserFromToken(jwt, TEST_LOCALE))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.MISSING_CLAIM_ON_PROVISION);
        verify(userRepository, never()).saveAndFlush(any());
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        return new Jwt(
                "testToken",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                TEST_HEADERS,
                claims
        );
    }

}