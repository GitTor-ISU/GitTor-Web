package api.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import api.dtos.AuthenticationDto;
import api.dtos.ErrorDto;
import api.dtos.LoginDto;
import api.dtos.RegisterDto;

/**
 * {@link AuthenticationController} test.
 */
public class AuthenticationControllerTest extends BasicControllerTest {
    @Value("${api.admin.username:admin}")
    private String adminUsername;
    @Value("${api.admin.password:password}")
    private String adminPassword;

    /**
     * {@link AuthenticationController#login} test.
     */
    @Nested
    public class Login {
        private static final String ENDPOINT = "/authenticate/login";

        @Test
        public void shouldLogin_whenAdmin() {
            // GIVEN: Login info for admin
            LoginDto login = LoginDto.builder()
                .username(adminUsername)
                .password(adminPassword)
                .build();
            HttpEntity<LoginDto> request = new HttpEntity<>(login, null);

            // WHEN: Login
            ResponseEntity<AuthenticationDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<AuthenticationDto>() {}
            );

            // THEN: Returns a bearer token
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getAccessToken()),
                () -> assertNotNull(responseEntity.getBody().getExpires()),
                () -> assertEquals("Bearer", responseEntity.getBody().getTokenType())
            );
        }

        @Test
        public void should401_whenIncorrectUsername() {
            // GIVEN: Incorrect username
            LoginDto login = LoginDto.builder()
                .username(adminUsername + "_incorrect")
                .password(adminPassword)
                .build();
            HttpEntity<LoginDto> request = new HttpEntity<>(login, null);

            // WHEN: Login
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unauthorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401_whenIncorrectPassword() {
            // GIVEN: Incorrect password
            LoginDto login = LoginDto.builder()
                .username(adminUsername)
                .password(adminPassword + "_incorrect")
                .build();
            HttpEntity<LoginDto> request = new HttpEntity<>(login, null);

            // WHEN: Login
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unauthorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }
    }

    /**
     * {@link AuthenticationController#register} test.
     */
    @Nested
    public class Register {
        private static final String ENDPOINT = "/authenticate/register";

        @Test
        public void shouldRegister_whenNewUser() {
            // GIVEN: User info
            RegisterDto login = RegisterDto.builder()
                .username("user_" + UUID.randomUUID())
                .password("password")
                .build();
            HttpEntity<RegisterDto> request = new HttpEntity<>(login, null);

            // WHEN: Register
            ResponseEntity<AuthenticationDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<AuthenticationDto>() {}
            );

            // THEN: Returns a bearer token
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getAccessToken()),
                () -> assertNotNull(responseEntity.getBody().getExpires()),
                () -> assertEquals("Bearer", responseEntity.getBody().getTokenType())
            );
        }

        @Test
        public void should400_whenEmptyUsername() {
            // GIVEN: Empty username
            RegisterDto login = RegisterDto.builder()
                .username("")
                .password("password")
                .build();
            HttpEntity<RegisterDto> request = new HttpEntity<>(login, null);

            // WHEN: Register
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Username must not be empty.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should400_whenEmptyPassword() {
            // GIVEN: Empty password
            RegisterDto login = RegisterDto.builder()
                .username("user_" + UUID.randomUUID())
                .password("")
                .build();
            HttpEntity<RegisterDto> request = new HttpEntity<>(login, null);

            // WHEN: Register
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Password must not be empty.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should409_whenDuplicateUsername() {
            // GIVEN: Existing username
            RegisterDto login = RegisterDto.builder()
                .username(adminUsername)
                .password(adminPassword + "_different")
                .build();
            HttpEntity<RegisterDto> request = new HttpEntity<>(login, null);

            // WHEN: Register
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds conflict
            assertAll(
                () -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals(
                    "Username '" + adminUsername + "' already exists.",
                    responseEntity.getBody().getMessage()
                )
            );
        }
    }
}
