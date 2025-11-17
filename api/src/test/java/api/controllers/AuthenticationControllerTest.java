package api.controllers;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import api.BasicContext;
import api.dtos.AuthenticationDto;
import api.dtos.ErrorDto;
import api.dtos.LoginDto;
import api.dtos.RegisterDto;

/**
 * {@link AuthenticationController} test.
 */
public class AuthenticationControllerTest extends BasicContext {
    /**
     * {@link AuthenticationController#login} test.
     */
    @Nested
    public class Login {
        private static final String ENDPOINT = "/authenticate/login";

        @Test
        public void shouldLogin_whenAdminUsername() {
            // GIVEN: Login info for admin
            LoginDto login = fixtureMonkey.giveMeBuilder(LoginDto.class).setNull(javaGetter(LoginDto::getEmail))
                .set(javaGetter(LoginDto::getUsername), adminUsername)
                .set(javaGetter(LoginDto::getPassword), adminPassword).sample();
            HttpEntity<LoginDto> request = new HttpEntity<>(login, null);

            // WHEN: Login
            ResponseEntity<AuthenticationDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT,
                HttpMethod.POST, request, new ParameterizedTypeReference<AuthenticationDto>() {});

            // THEN: Returns a bearer token
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getAccessToken()),
                () -> assertNotNull(responseEntity.getBody().getExpires()),
                () -> assertEquals("Bearer", responseEntity.getBody().getTokenType()));
        }

        @Test
        public void shouldLogin_whenAdminEmail() {
            // GIVEN: Login info for admin
            LoginDto login = fixtureMonkey.giveMeBuilder(LoginDto.class).set(javaGetter(LoginDto::getEmail), adminEmail)
                .setNull(javaGetter(LoginDto::getUsername)).set(javaGetter(LoginDto::getPassword), adminPassword)
                .sample();
            HttpEntity<LoginDto> request = new HttpEntity<>(login, null);

            // WHEN: Login
            ResponseEntity<AuthenticationDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT,
                HttpMethod.POST, request, new ParameterizedTypeReference<AuthenticationDto>() {});

            // THEN: Returns a bearer token
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getAccessToken()),
                () -> assertNotNull(responseEntity.getBody().getExpires()),
                () -> assertEquals("Bearer", responseEntity.getBody().getTokenType()));
        }

        @Test
        public void should401_whenIncorrectUsername() {
            // GIVEN: Incorrect username
            LoginDto login = fixtureMonkey.giveMeBuilder(LoginDto.class)
                .set(javaGetter(LoginDto::getPassword), adminPassword).sample();
            HttpEntity<LoginDto> request = new HttpEntity<>(login, null);

            // WHEN: Login
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST, request,
                new ParameterizedTypeReference<String>() {});

            // THEN: Responds unauthorized
            assertAll(() -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()));
        }

        @Test
        public void should401_whenIncorrectPassword() {
            // GIVEN: Incorrect password
            LoginDto login = fixtureMonkey.giveMeBuilder(LoginDto.class)
                .set(javaGetter(LoginDto::getUsername), adminUsername).sample();
            HttpEntity<LoginDto> request = new HttpEntity<>(login, null);

            // WHEN: Login
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST, request,
                new ParameterizedTypeReference<String>() {});

            // THEN: Responds unauthorized
            assertAll(() -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()));
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
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            HttpEntity<RegisterDto> request = new HttpEntity<>(register, null);

            // WHEN: Register
            ResponseEntity<AuthenticationDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT,
                HttpMethod.POST, request, new ParameterizedTypeReference<AuthenticationDto>() {});

            // THEN: Returns a bearer token
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getAccessToken()),
                () -> assertNotNull(responseEntity.getBody().getExpires()),
                () -> assertEquals("Bearer", responseEntity.getBody().getTokenType()));
        }

        @Test
        public void should400_whenEmptyEmail() {
            // GIVEN: Empty email
            RegisterDto register = fixtureMonkey.giveMeBuilder(RegisterDto.class)
                .set(javaGetter(RegisterDto::getEmail), "").validOnly(false).sample();
            HttpEntity<RegisterDto> request = new HttpEntity<>(register, null);

            // WHEN: Register
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds bad request
            assertAll(() -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Email size must be between 3 and 255.", responseEntity.getBody().getMessage()));
        }

        @Test
        public void should400_whenEmptyUsername() {
            // GIVEN: Empty username
            RegisterDto register = fixtureMonkey.giveMeBuilder(RegisterDto.class)
                .set(javaGetter(RegisterDto::getUsername), "").validOnly(false).sample();
            HttpEntity<RegisterDto> request = new HttpEntity<>(register, null);

            // WHEN: Register
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds bad request
            assertAll(() -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Username size must be between 3 and 20.", responseEntity.getBody().getMessage()));
        }

        @Test
        public void should400_whenEmptyPassword() {
            // GIVEN: Empty password
            RegisterDto register = fixtureMonkey.giveMeBuilder(RegisterDto.class)
                .set(javaGetter(RegisterDto::getPassword), "").validOnly(false).sample();
            HttpEntity<RegisterDto> request = new HttpEntity<>(register, null);

            // WHEN: Register
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds bad request
            assertAll(() -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Password size must be between 8 and 72.", responseEntity.getBody().getMessage()));
        }

        @Test
        public void should400_whenInvalidUsername() {
            // GIVEN: Empty username
            RegisterDto register = fixtureMonkey.giveMeBuilder(RegisterDto.class)
                .set(javaGetter(RegisterDto::getUsername), "invalidusername!").validOnly(false).sample();
            HttpEntity<RegisterDto> request = new HttpEntity<>(register, null);

            // WHEN: Register
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds bad request
            assertAll(() -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Username must match \"^[a-zA-Z0-9_-]*$\".", responseEntity.getBody().getMessage()));
        }

        @Test
        public void should400_whenInvalidEmail() {
            // GIVEN: Empty email
            RegisterDto register = fixtureMonkey.giveMeBuilder(RegisterDto.class)
                .set(javaGetter(RegisterDto::getEmail), "invalidemail!").validOnly(false).sample();
            HttpEntity<RegisterDto> request = new HttpEntity<>(register, null);

            // WHEN: Register
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds bad request
            assertAll(() -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Email must be a well-formed email address.",
                    responseEntity.getBody().getMessage()));
        }

        @Test
        public void should409_whenDuplicateEmail() {
            // GIVEN: Existing email
            RegisterDto register = fixtureMonkey.giveMeBuilder(RegisterDto.class)
                .set(javaGetter(RegisterDto::getEmail), adminEmail).sample();
            HttpEntity<RegisterDto> request = new HttpEntity<>(register, null);

            // WHEN: Register
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds conflict
            assertAll(() -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Email '" + adminEmail + "' already in use.",
                    responseEntity.getBody().getMessage()));
        }

        @Test
        public void should409_whenDuplicateUsername() {
            // GIVEN: Existing username
            RegisterDto register = fixtureMonkey.giveMeBuilder(RegisterDto.class)
                .set(javaGetter(RegisterDto::getUsername), adminUsername).sample();
            HttpEntity<RegisterDto> request = new HttpEntity<>(register, null);

            // WHEN: Register
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds conflict
            assertAll(() -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User '" + adminUsername + "' already exists.",
                    responseEntity.getBody().getMessage()));
        }
    }
}
