package api.controllers.users;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;

import api.BasicContext;
import api.controllers.AuthenticationController;
import api.dtos.AuthenticationDto;
import api.dtos.AuthorityDto;
import api.dtos.ErrorDto;
import api.dtos.LoginDto;
import api.dtos.RegisterDto;
import api.dtos.UserDto;
import api.entities.User;
import api.services.TokenService;
import api.services.UserService;
import io.jsonwebtoken.Jwts;

/**
 * {@link UserController} test.
 */
public class UserControllerTest extends BasicContext {
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private AuthenticationController authenticationController;

    @Value("${jwt.token.expires-minutes:1440}")
    private int expires;
    @Value("${pagination.default-page-size:10}")
    private int defaultPageSize;
    @Value("${pagination.max-page-size:100}")
    private int maxPageSize;

    /**
     * {@link UserController#getMe} test.
     */
    @Nested
    public class GetMe {
        private static final String ENDPOINT = "/users/me";

        @Test
        public void shouldGetMe_whenJwtAuthentication() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get me
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns me
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getId()),
                () -> assertEquals(username, responseEntity.getBody().getUsername())
            );
        }

        @Test
        public void shouldGetMe_whenBasicAuthentication() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            String password = register.getPassword();
            authenticationController.register(register);

            // GIVEN: Basic authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get me
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns me
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getId()),
                () -> assertEquals(username, responseEntity.getBody().getUsername())
            );
        }

        @Test
        public void should401_whenNoAuthentication() {
            // GIVEN: No authentication
            // WHEN: Get me
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, null, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unathorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401_whenJwtAuthenticationExpires() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: JWT authentication
            AuthenticationDto auth = tokenService.generateToken(
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList())
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Wait till token expires
            instant = instant.plus(expires, ChronoUnit.MINUTES).plus(1, ChronoUnit.SECONDS);

            // WHEN: Get me
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unathorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401_whenJwtAuthenticationInvalid() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: Invalid JWT authentication
            String invalidToken = Jwts.builder()
                    .subject(username)
                    .signWith(Jwts.SIG.HS512.key().build())
                    .compact();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(invalidToken);
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get me
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unathorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401_whenBasicAuthenticationIncorrectUsername() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            String password = register.getPassword();
            authenticationController.register(register);

            // GIVEN: Basic authentication with incorrect username
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username + "_incorrect", password);
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get me
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Responds unathorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401_whenBasicAuthenticationIncorrectPassword() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            String password = register.getPassword();
            authenticationController.register(register);

            // GIVEN: Basic authentication with incorrect password
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password + "_incorrect");
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get me
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Responds unathorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }
    }

    /**
     * {@link UserController#getMyAuthorities} test.
     */
    @Nested
    public class GetMyAuthorities {
        private static final String ENDPOINT = "/users/me/authorities";

        @Test
        public void shouldGetMyAuthoritiesWhenJwtAuthentication() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get my authorities
            ResponseEntity<List<AuthorityDto>> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<AuthorityDto>>() {}
            );

            // THEN: Returns my authorities
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody())
            );
        }

        @Test
        public void shouldGetMyAuthoritiesWhenBasicAuthentication() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            String password = register.getPassword();
            authenticationController.register(register);

            // GIVEN: Basic authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get my authorities
            ResponseEntity<List<AuthorityDto>> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<AuthorityDto>>() {}
            );

            // THEN: Returns my authorities
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401WhenNoAuthentication() {
            // GIVEN: No authentication

            // WHEN: Get my authorities
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unauthorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401WhenJwtAuthenticationExpires() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: JWT authentication
            AuthenticationDto auth = tokenService.generateToken(
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList())
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Wait till token expires
            instant = instant.plus(expires, ChronoUnit.MINUTES).plus(1, ChronoUnit.SECONDS);

            // WHEN: Get my authorities
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unauthorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401WhenJwtAuthenticationInvalid() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: Invalid JWT authentication
            String invalidToken = Jwts.builder()
                .subject(username)
                .signWith(Jwts.SIG.HS512.key().build())
                .compact();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(invalidToken);
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get my authorities
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unauthorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401WhenBasicAuthenticationIncorrectUsername() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            String password = register.getPassword();
            authenticationController.register(register);

            // GIVEN: Basic authentication with incorrect username
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username + "_incorrect", password);
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get my authorities
            ResponseEntity<List<AuthorityDto>> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<AuthorityDto>>() {}
            );

            // THEN: Responds unauthorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401WhenBasicAuthenticationIncorrectPassword() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            String password = register.getPassword();
            authenticationController.register(register);

            // GIVEN: Basic authentication with incorrect password
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password + "_incorrect");
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get my authorities
            ResponseEntity<List<AuthorityDto>> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<AuthorityDto>>() {}
            );

            // THEN: Responds unauthorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void shouldReturnEmptyAuthoritiesWhenUserHasDefaultUserRole() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: Remove default roles and assign empty role
            User user = userService.getWithRoles(username);
            int expectedAuthorityCount = user.getRoles().stream()
                .mapToInt(role -> role.getAuthorities().size())
                .sum();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get my authorities
            ResponseEntity<List<AuthorityDto>> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<AuthorityDto>>() {}
            );

            // THEN: Returns empty or minimal list (matches default USER role)
            List<AuthorityDto> authorities = responseEntity.getBody();
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(authorities),
                () -> assertEquals(expectedAuthorityCount, authorities.size(),
                    "Should return authorities matching default USER role")
            );
        }

        @Test
        public void shouldReturnPopulatedAuthoritiesWhenUserHasAdminRole() {
            // GIVEN: Use existing admin user from BasicContext
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get my authorities
            ResponseEntity<List<AuthorityDto>> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<AuthorityDto>>() {}
            );

            // THEN: Returns populated list with admin authorities
            List<AuthorityDto> authorities = responseEntity.getBody();
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(authorities),
                () -> assertTrue(authorities.size() > 0, "Admin should have at least one authority"),
                () -> assertTrue(
                    authorities.stream().allMatch(a -> a.getId() != null && a.getAuthority() != null),
                    "All authorities should have valid structure"
                )
            );
        }
    }

    /**
     * {@link UserController#updateMe} test.
     */
    @Nested
    public class UpdateMe {
        private static final String ENDPOINT = "/users/me";

        @Test
        public void shouldUpdateMe_whenSomeNullValues() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: Updated user info, but some values null (id, username)
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                    .setNull(javaGetter(UserDto::getId))
                    .setNull(javaGetter(UserDto::getUsername))
                    .sample();
            String firstname = updated.getFirstname();
            String lastname = updated.getLastname();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // WHEN: Update me
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns me
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getId()),
                () -> assertEquals(username, responseEntity.getBody().getUsername()),
                () -> assertEquals(firstname, responseEntity.getBody().getFirstname()),
                () -> assertEquals(lastname, responseEntity.getBody().getLastname())
            );
        }

        @Test
        public void shouldUpdateMe_whenIdIncorrect() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: Updated user info, but with an incorrect ID
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                    .setNull(javaGetter(UserDto::getUsername))
                    .sample();
            String firstname = updated.getFirstname();
            String lastname = updated.getLastname();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // WHEN: Update me
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns me
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getId()),
                () -> assertEquals(username, responseEntity.getBody().getUsername()),
                () -> assertEquals(firstname, responseEntity.getBody().getFirstname()),
                () -> assertEquals(lastname, responseEntity.getBody().getLastname())
            );
        }

        @Test
        public void shouldUpdateMe_whenUsernameChanged() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: Updated user info with new username
            UserDto updated = fixtureMonkey.giveMeOne(UserDto.class);
            String newUsername = updated.getUsername();
            String firstname = updated.getFirstname();
            String lastname = updated.getLastname();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // WHEN: Update me
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns me
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getId()),
                () -> assertEquals(newUsername, responseEntity.getBody().getUsername()),
                () -> assertEquals(firstname, responseEntity.getBody().getFirstname()),
                () -> assertEquals(lastname, responseEntity.getBody().getLastname())
            );
        }

        @Test
        public void shouldUpdateMe_whenUsernameNotChanged() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: Updated user info with same username
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                                .set(javaGetter(UserDto::getUsername), username)
                                .sample();
            String firstname = updated.getFirstname();
            String lastname = updated.getLastname();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // WHEN: Update me
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns me
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertNotNull(responseEntity.getBody().getId()),
                () -> assertEquals(username, responseEntity.getBody().getUsername()),
                () -> assertEquals(firstname, responseEntity.getBody().getFirstname()),
                () -> assertEquals(lastname, responseEntity.getBody().getLastname())
            );
        }

        @Test
        public void should400_whenUsernameEmpty() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: Updated username to empty
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                                .set(javaGetter(UserDto::getUsername), "")
                                .validOnly(false)
                                .sample();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // WHEN: Update me
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Username size must be between 3 and 20.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should400_whenUsernameInvalid() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: Updated username to empty
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                                .set(javaGetter(UserDto::getUsername), "invalidusername!")
                                .validOnly(false)
                                .sample();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // WHEN: Update me
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Username must match \"^[a-zA-Z0-9_-]*$\".", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should409_whenDuplicateUsername() {
            // GIVEN: Two new users registered
            RegisterDto register1 = fixtureMonkey.giveMeOne(RegisterDto.class);
            RegisterDto register2 = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username2 = register2.getUsername();
            AuthenticationDto auth = authenticationController.register(register1);
            authenticationController.register(register2);

            // GIVEN: Updated user info with username of other user
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                                .set(javaGetter(UserDto::getUsername), username2)
                                .sample();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // WHEN: Update me
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds conflict
            assertAll(
                () -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals(
                    "User '" + username2 + "' already exists.",
                    responseEntity.getBody().getMessage()
                )
            );
        }
    }

    /**
     * {@link UserController#updateMyPassword} test.
     */
    @Nested
    public class UpdateMyPassword {
        private static final String ENDPOINT = "/users/me/password";

        @Test
        public void shouldUpdateMyPassword() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            String oldPassword = register.getPassword();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: New password is different
            String newPassword = "newPassword";

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<String> request = new HttpEntity<>(newPassword, headers);

            // WHEN: Update my password
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Can login with new password, but not old
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertDoesNotThrow(
                    () -> authenticationController.login(
                        fixtureMonkey.giveMeBuilder(LoginDto.class)
                            .set(javaGetter(LoginDto::getUsername), username)
                            .set(javaGetter(LoginDto::getPassword), newPassword)
                            .sample()
                    )
                ),
                () -> assertThrows(
                    BadCredentialsException.class,
                    () -> authenticationController.login(
                        fixtureMonkey.giveMeBuilder(LoginDto.class)
                            .set(javaGetter(LoginDto::getUsername), username)
                            .set(javaGetter(LoginDto::getPassword), oldPassword)
                            .sample()
                    )
                )
            );
        }

        @Test
        public void should400_whenPasswordUnchanged() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String oldPassword = register.getPassword();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: New password is same
            String newPassword = oldPassword;

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<String> request = new HttpEntity<>(newPassword, headers);

            // WHEN: Update my password
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("New password matches existing password.", responseEntity.getBody().getMessage())
            );
        }
    }

    /**
     * {@link UserController#deleteMe} test.
     */
    @Nested
    public class DeleteMe {
        private static final String ENDPOINT = "/users/me";

        @Test
        public void shouldDeleteMe_whenJwtAuthentication() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Delete me
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Returns nothing and user doesn't exist
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertFalse(
                    userService.exists(username),
                    "Unexpected user '" + username + "' found"
                )
            );
        }

        @Test
        public void shouldDeleteMe_whenBasicAuthentication() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            String password = register.getPassword();
            authenticationController.register(register);

            // GIVEN: Basic authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Delete me
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Returns nothing and user doesn't exist
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertFalse(
                    userService.exists(username),
                    "Unexpected user '" + username + "' found"
                )
            );
        }

        @Test
        public void should401_whenNoAuthentication() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: No authentication
            // WHEN: Delete me
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, null, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unathorized and user still exists
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertTrue(
                    userService.exists(username),
                    "Expected user '" + username + "' not found"
                )
            );
        }

        @Test
        public void should401_whenJwtAuthenticationInvalid() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: Invalid JWT authentication
            String invalidToken = Jwts.builder()
                    .subject(username)
                    .signWith(Jwts.SIG.HS512.key().build())
                    .compact();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(invalidToken);
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Delete me
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unathorized
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @Test
        public void should401_whenJwtAuthenticationExpires() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: JWT authentication
            AuthenticationDto auth = tokenService.generateToken(
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList())
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Wait till token expires
            instant = instant.plus(expires, ChronoUnit.MINUTES).plus(1, ChronoUnit.SECONDS);

            // WHEN: Delete me
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Responds unathorized and user still exists
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertTrue(
                    userService.exists(username),
                    "Expected user '" + username + "' not found"
                )
            );
        }

        @Test
        public void should401_whenBasicAuthenticationIncorrectUsername() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            String password = register.getPassword();
            authenticationController.register(register);

            // GIVEN: Basic authentication with incorrect username
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username + "_incorrect", password);
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Delete me
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Responds unathorized and user still exists
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertTrue(
                    userService.exists(username),
                    "Expected user '" + username + "' not found"
                )
            );
        }

        @Test
        public void should401_whenBasicAuthenticationIncorrectPassword() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            String password = register.getPassword();
            authenticationController.register(register);

            // GIVEN: Basic authentication with incorrect password
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password + "_incorrect");
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Delete me
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Responds unathorized
            // THEN: Responds unathorized and user still exists
            assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertTrue(
                    userService.exists(username),
                    "Expected user '" + username + "' not found"
                )
            );
        }

        @Test
        public void should409_whenLastAdminUser() {
            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Delete admin user
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds conflict
            assertAll(
                () -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Last admin user cannot be removed from the system.",
                            responseEntity.getBody().getMessage())
            );
        }
    }

    /**
     * {@link UserController#getUsers} test.
     */
    @Nested
    public class GetUsers {
        private static final String ENDPOINT = "/users";

        @Test
        public void shouldGetUsers_whenNoQueryParameters() {
            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get users
            ResponseEntity<List<UserDto>> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<List<UserDto>>() {}
            );

            // THEN: Returns users
            List<UserDto> users = responseEntity.getBody();
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(users),
                () -> assertTrue(users.size() > 0, "Returned users should include at least me"),
                () -> assertTrue(
                    users.size() <= defaultPageSize,
                    "Returned user count should not exceed default page size"
                ),
                () -> assertTrue(
                    IntStream.range(1, users.size()).allMatch(i -> users.get(i).getId() >= users.get(i - 1).getId()),
                    "Users should be sorted by ID ascending"
                )
            );
        }

        @Test
        public void shouldGetUsers_whenQueryParameters() {
            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Request with paramters
            int size = 5;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .queryParam("page", 0)
                .queryParam("size", size)
                .build()
                .toUri();

            // WHEN: Get users
            ResponseEntity<List<UserDto>> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<List<UserDto>>() {}
            );

            // THEN: Returns users
            List<UserDto> users = responseEntity.getBody();
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(users),
                () -> assertTrue(users.size() > 0, "Returned users should include at least me"),
                () -> assertTrue(users.size() <= size, "Returned user count should not exceed requested size"),
                () -> assertTrue(
                    IntStream.range(1, users.size()).allMatch(i -> users.get(i).getId() >= users.get(i - 1).getId()),
                    "Users should be sorted by ID ascending"
                )
            );
        }

        @Test
        public void shouldGetUsers_whenQueryParametersExceedLimit() {
            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Request with paramters
            int size = maxPageSize + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .queryParam("page", 0)
                .queryParam("size", size)
                .build()
                .toUri();

            // WHEN: Get users
            ResponseEntity<List<UserDto>> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<List<UserDto>>() {}
            );

            // THEN: Returns users
            List<UserDto> users = responseEntity.getBody();
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(users),
                () -> assertTrue(users.size() > 0, "Returned users should include at least me"),
                () -> assertTrue(users.size() <= maxPageSize, "Returned user count should not exceed max page size"),
                () -> assertTrue(
                    IntStream.range(1, users.size()).allMatch(i -> users.get(i).getId() >= users.get(i - 1).getId()),
                    "Users should be sorted by ID ascending"
                )
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: User authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get users
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Returns users
            assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage())
            );
        }
    }

    /**
     * {@link UserController#getUser} test.
     */
    @Nested
    public class GetUser {
        private static final String ENDPOINT = "/users/{id}";

        @Test
        public void shouldGetUser() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Get user
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns user
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(user.getId(), responseEntity.getBody().getId()),
                () -> assertEquals(username, responseEntity.getBody().getUsername())
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Get user
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds forbidden
            assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should404_whenNonexistent() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Wrong user id in path
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Get user
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User " + wrongId + " not found.", responseEntity.getBody().getMessage())
            );
        }
    }

    /**
     * {@link UserController#updateUser} test.
     */
    @Nested
    public class UpdateUser {
        private static final String ENDPOINT = "/users/{id}";

        @Test
        public void shouldUpdateUser_whenSomeNullValues() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Updated user info, but some values null (id, username)
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                    .setNull(javaGetter(UserDto::getId))
                    .setNull(javaGetter(UserDto::getUsername))
                    .sample();
            String firstname = updated.getFirstname();
            String lastname = updated.getLastname();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns user
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(user.getId(), responseEntity.getBody().getId()),
                () -> assertEquals(username, responseEntity.getBody().getUsername()),
                () -> assertEquals(firstname, responseEntity.getBody().getFirstname()),
                () -> assertEquals(lastname, responseEntity.getBody().getLastname())
            );
        }

        @Test
        public void shouldUpdateUser_whenBodyIdIncorrect() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Updated user info, but with an incorrect ID
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                    .setNull(javaGetter(UserDto::getUsername))
                    .sample();
            String firstname = updated.getFirstname();
            String lastname = updated.getLastname();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns user
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(user.getId(), responseEntity.getBody().getId()),
                () -> assertEquals(username, responseEntity.getBody().getUsername()),
                () -> assertEquals(firstname, responseEntity.getBody().getFirstname()),
                () -> assertEquals(lastname, responseEntity.getBody().getLastname())
            );
        }

        @Test
        public void shouldUpdateUser_whenUsernameChanged() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Updated user info with new username
            UserDto updated = fixtureMonkey.giveMeOne(UserDto.class);
            String newUsername = updated.getUsername();
            String firstname = updated.getFirstname();
            String lastname = updated.getLastname();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns user
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(user.getId(), responseEntity.getBody().getId()),
                () -> assertEquals(newUsername, responseEntity.getBody().getUsername()),
                () -> assertEquals(firstname, responseEntity.getBody().getFirstname()),
                () -> assertEquals(lastname, responseEntity.getBody().getLastname())
            );
        }

        @Test
        public void shouldUpdateUser_whenUsernameNotChanged() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Updated user info with same username
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                                .set("username", username)
                                .sample();
            String firstname = updated.getFirstname();
            String lastname = updated.getLastname();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user
            ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<UserDto>() {}
            );

            // THEN: Returns user
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(user.getId(), responseEntity.getBody().getId()),
                () -> assertEquals(username, responseEntity.getBody().getUsername()),
                () -> assertEquals(firstname, responseEntity.getBody().getFirstname()),
                () -> assertEquals(lastname, responseEntity.getBody().getLastname())
            );
        }

        @Test
        public void should400_whenUsernameEmpty() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Updated username to empty
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                                .set(javaGetter(UserDto::getUsername), "")
                                .validOnly(false)
                                .sample();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Username size must be between 3 and 20.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Any userDto
            UserDto updated = fixtureMonkey.giveMeOne(UserDto.class);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds forbidden
            assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should404_whenNonexistent() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Updated user info, but some values null (id, username)
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                    .setNull(javaGetter(UserDto::getId))
                    .setNull(javaGetter(UserDto::getUsername))
                    .sample();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // GIVEN: Wrong user id in path
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Update user
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User " + wrongId + " not found.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should409_whenDuplicateUsername() {
            // GIVEN: Two new users registered
            RegisterDto register1 = fixtureMonkey.giveMeOne(RegisterDto.class);
            RegisterDto register2 = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username1 = register1.getUsername();
            String username2 = register2.getUsername();
            authenticationController.register(register1);
            authenticationController.register(register2);
            User user1 = userService.get(username1);

            // GIVEN: Updated user info with username of other user
            UserDto updated = fixtureMonkey.giveMeBuilder(UserDto.class)
                                .set(javaGetter(UserDto::getUsername), username2)
                                .sample();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<UserDto> request = new HttpEntity<>(updated, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user1.getId())
                .toUri();

            // WHEN: Update user
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds conflict
            assertAll(
                () -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals(
                    "User '" + username2 + "' already exists.",
                    responseEntity.getBody().getMessage()
                )
            );
        }
    }

    /**
     * {@link UserController#deleteUser} test.
     */
    @Nested
    public class DeleteUser {
        private static final String ENDPOINT = "/users/{id}";

        @Test
        public void shouldDeleteUser() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Delete user
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Returns nothing and user doesn't exist
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertFalse(
                    userService.exists(username),
                    "Unexpected user '" + username + "' found"
                )
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Delete user
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds forbidden
            assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should404_whenNonexistent() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);
            User user = userService.get(username);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Wrong user id in path
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Delete user
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User " + wrongId + " not found.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should409_whenLastAdminUser() {
            // GIVEN: Admin user exists
            User adminUser = userService.get("admin");

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(adminUser.getId())
                .toUri();

            // WHEN: Delete user
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds conflict
            assertAll(
                () -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Last admin user cannot be removed from the system.",
                            responseEntity.getBody().getMessage())
            );
        }
    }
}
