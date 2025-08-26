package api.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import api.dtos.AuthenticationDto;
import api.dtos.AuthorityDto;
import api.dtos.ErrorDto;
import api.dtos.RegisterDto;
import api.entities.Authority;
import api.mapper.AuthorityMapper;
import api.services.AuthorityService;

/**
 * {@link AuthorityController} test.
 */
public class AuthorityControllerTest extends BasicControllerTest {
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private AuthenticationController authenticationController;
    @Autowired
    private AuthorityMapper authorityMapper;

    /**
     * {@link AuthorityController#getAuthorities} test.
     */
    @Nested
    public class GetAuthorities {
        private static final String ENDPOINT = "/authorities";

        @Test
        public void shouldGetAuthorities() {
            // GIVEN: New authority exists
            String newAuthorityName = "authority_" + UUID.randomUUID();
            authorityService.save(Authority.builder().authority(newAuthorityName).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get authorities
            ResponseEntity<List<AuthorityDto>> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<List<AuthorityDto>>() {}
            );

            // THEN: New authority should be in list
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertTrue(
                    responseEntity.getBody().stream().anyMatch(a -> newAuthorityName.equals(a.getAuthority())),
                    "Expected authority '" + newAuthorityName + "' not found"
                )
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: User authentication header
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username("user_" + UUID.randomUUID())
                    .password("password")
                    .build()
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get authorities
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds forbidden
            assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage())
            );
        }
    }

    /**
     * {@link AuthorityController#getAuthority} test.
     */
    @Nested
    public class GetAuthority {
        private static final String ENDPOINT = "/authorities/{id}";

        @Test
        public void shouldGetAuthority() {
            // GIVEN: New authority exists
            String newAuthorityName = "authority_" + UUID.randomUUID();
            Authority newAuthority = authorityService.save(Authority.builder().authority(newAuthorityName).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(newAuthority.getId())
                .toUri();

            // WHEN: Get new authority
            ResponseEntity<AuthorityDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<AuthorityDto>() {}
            );

            // THEN: New authority should be returned
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(authorityMapper.toDto(newAuthority), responseEntity.getBody())
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New authority exists
            String newAuthorityName = "authority_" + UUID.randomUUID();
            Authority newAuthority = authorityService.save(Authority.builder().authority(newAuthorityName).build());

            // GIVEN: User authentication header
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username("user_" + UUID.randomUUID())
                    .password("password")
                    .build()
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(newAuthority.getId())
                .toUri();

            // WHEN: Get new authority
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
            // GIVEN: New authority exists
            String newAuthorityName = "authority_" + UUID.randomUUID();
            Authority newAuthority = authorityService.save(Authority.builder().authority(newAuthorityName).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Wrong authority id
            int wrongId = newAuthority.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Get new authority at id plus 1
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Authority " + wrongId + " not found.", responseEntity.getBody().getMessage())
            );
        }
    }
}
