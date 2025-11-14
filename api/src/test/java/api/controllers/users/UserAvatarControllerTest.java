package api.controllers.users;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import api.BasicContext;
import api.controllers.AuthenticationController;
import api.dtos.AuthenticationDto;
import api.dtos.ErrorDto;
import api.dtos.RegisterDto;
import api.entities.S3Object;
import api.entities.User;
import api.exceptions.StorageException;
import api.services.S3ObjectService;
import api.services.UserService;

/**
 * {@link UserAvatarController} test.
 */
public class UserAvatarControllerTest extends BasicContext {
    @Autowired
    private AuthenticationController authenticationController;
    @Autowired
    private UserService userService;
    @Autowired
    private S3ObjectService s3ObjectService;

    @Value("${api.s3.avatar.max}")
    private long maxAvatarSize;

    /**
     * {@link UserAvatarController#getMyAvatar} test.
     */
    @Nested
    public class GetMyAvatar {
        private static final String ENDPOINT = "/users/me/avatar";

        @ParameterizedTest
        @CsvSource({
            ".png, image/png",
            ".jpeg, image/jpeg",
            ".jpg, image/jpeg",
            ".gif, image/gif",
            ".svg, image/svg+xml"
        })
        public void shouldGetMyAvatar(String extension, String expectedMediaType) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders putHeaders = new HttpHeaders();
            putHeaders.setBearerAuth(auth.getAccessToken());
            putHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> putRequest = new HttpEntity<>(body, putHeaders);

            // GIVEN: Updated avatar
            testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, putRequest, new ParameterizedTypeReference<String>() {}
            );

            // GIVEN: JWT authentication
            HttpHeaders getHeaders = new HttpHeaders();
            getHeaders.setBearerAuth(auth.getAccessToken());

            // WHEN: Get avatar
            HttpEntity<Void> getRequest = new HttpEntity<>(null, getHeaders);
            ResponseEntity<Resource> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, getRequest, new ParameterizedTypeReference<Resource>() {}
            );

            // THEN: Returns avatar photo
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertArrayEquals(content, responseEntity.getBody().getContentAsByteArray()),
                () -> assertEquals(
                    MediaType.parseMediaType(expectedMediaType),
                    responseEntity.getHeaders().getContentType()
                )
            );
        }

        @ParameterizedTest
        @CsvSource({
            ".png, image/png",
            ".jpeg, image/jpeg",
            ".jpg, image/jpeg",
            ".gif, image/gif",
            ".svg, image/svg+xml"
        })
        public void shouldGetMyAvatar_whenAvatarChanged(String extension, String expectedMediaType) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders putHeaders = new HttpHeaders();
            putHeaders.setBearerAuth(auth.getAccessToken());
            putHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] oldContent = "old-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(oldContent) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> putRequest = new HttpEntity<>(body, putHeaders);

            // GIVEN: Updated avatar to old photo
            testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, putRequest, new ParameterizedTypeReference<String>() {}
            );

            // GIVEN: Update avatar photo
            byte[] newContent = "new-image-content".getBytes(StandardCharsets.UTF_8);
            avatar = new ByteArrayResource(newContent) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            putRequest = new HttpEntity<>(body, putHeaders);

            // GIVEN: Updated avatar to new photo
            testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, putRequest, new ParameterizedTypeReference<String>() {}
            );

            // GIVEN: JWT authentication
            HttpHeaders getHeaders = new HttpHeaders();
            getHeaders.setBearerAuth(auth.getAccessToken());

            // WHEN: Get avatar
            HttpEntity<Void> getRequest = new HttpEntity<>(null, getHeaders);
            ResponseEntity<Resource> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, getRequest, new ParameterizedTypeReference<Resource>() {}
            );

            // THEN: Returns new avatar photo
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertArrayEquals(newContent, responseEntity.getBody().getContentAsByteArray()),
                () -> assertEquals(
                    MediaType.parseMediaType(expectedMediaType),
                    responseEntity.getHeaders().getContentType()
                )
            );
        }

        @Test
        public void should404_whenNonexistent() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());

            // WHEN: Get avatar
            HttpEntity<Void> request = new HttpEntity<>(null, headers);
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User '" + username + "' avatar not found.", responseEntity.getBody().getMessage())
            );
        }
    }

    /**
     * {@link UserAvatarController#updateMyAvatar} test.
     */
    @Nested
    public class UpdateMyAvatar {
        private static final String ENDPOINT = "/users/me/avatar";

        @ParameterizedTest
        @CsvSource({".png", ".jpeg", ".jpg", ".gif", ".svg"})
        public void shouldUpdateMyAvatar(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update my avatar
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Returns nothing and file is in storage
            String username = register.getUsername();
            User user = userService.get(username);
            S3Object avatarObject = user.getAvatar();
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertArrayEquals(content, s3ObjectService.download(avatarObject).readAllBytes())
            );
        }

        @ParameterizedTest
        @CsvSource({".csv", ".txt", ".xml", "''"})
        public void should400_whenMediaTypeInvalid(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update my avatar
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals(
                    "Only PNG, JPEG, SVG and GIF images are allowed.", responseEntity.getBody().getMessage()
                )
            );
        }

        @ParameterizedTest
        @CsvSource({".png", ".jpeg", ".jpg", ".gif", ".svg"})
        public void should400_whenFileNull(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Empty avatar photo
            byte[] content = "".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update my avatar
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals(
                    "File must not be empty.", responseEntity.getBody().getMessage()
                )
            );
        }

        @ParameterizedTest
        @CsvSource({".png", ".jpeg", ".jpg", ".gif", ".svg"})
        public void should400_whenTooLarge(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Max size avatar photo
            int size = (int) (maxAvatarSize + 1);
            byte[] content = new byte[size];
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update my avatar
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals(
                    "File size exceeds limit (" + maxAvatarSize + " bytes).", responseEntity.getBody().getMessage()
                )
            );
        }
    }

    /**
     * {@link UserAvatarController#deleteMyAvatar} test.
     */
    @Nested
    public class DeleteMyAvatar {
        private static final String ENDPOINT = "/users/me/avatar";

        @ParameterizedTest
        @CsvSource({".png", ".jpeg", ".jpg", ".gif", ".svg"})
        public void shouldDeleteMyAvatar(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders putHeaders = new HttpHeaders();
            putHeaders.setBearerAuth(auth.getAccessToken());
            putHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> putRequest = new HttpEntity<>(body, putHeaders);

            // GIVEN: Updated avatar
            testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, putRequest, new ParameterizedTypeReference<String>() {}
            );
            String username = register.getUsername();
            User user = userService.get(username);
            S3Object avatarObject = user.getAvatar();

            // GIVEN: JWT authentication
            HttpHeaders deleteHeaders = new HttpHeaders();
            deleteHeaders.setBearerAuth(auth.getAccessToken());

            // WHEN: Delete avatar
            HttpEntity<Void> getRequest = new HttpEntity<>(null, deleteHeaders);
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, getRequest, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Deletes avatar photo
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertThrows(StorageException.class, () -> s3ObjectService.download(avatarObject))
            );
        }

        @Test
        public void shouldDeleteAvatar_whenUserDeleted() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders putHeaders = new HttpHeaders();
            putHeaders.setBearerAuth(auth.getAccessToken());
            putHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + ".png";
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> putRequest = new HttpEntity<>(body, putHeaders);

            // GIVEN: Updated avatar
            testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, putRequest, new ParameterizedTypeReference<String>() {}
            );
            String username = register.getUsername();
            User user = userService.get(username);
            S3Object avatarObject = user.getAvatar();

            // GIVEN: JWT authentication
            HttpHeaders deleteHeaders = new HttpHeaders();
            deleteHeaders.setBearerAuth(auth.getAccessToken());

            // WHEN: Delete user
            HttpEntity<Void> request = new HttpEntity<>(null, deleteHeaders);
            testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Deletes avatar photo
            assertThrows(StorageException.class, () -> s3ObjectService.download(avatarObject));
        }

        @Test
        public void should404_whenNonexistent() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());

            // WHEN: Delete avatar
            HttpEntity<Void> request = new HttpEntity<>(null, headers);
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.DELETE, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User '" + username + "' avatar not found.", responseEntity.getBody().getMessage())
            );
        }
    }

    /**
     * {@link UserAvatarController#getUserAvatar} test.
     */
    @Nested
    public class GetUserAvatar {
        private static final String ENDPOINT = "/users/{userId}/avatar";

        @ParameterizedTest
        @CsvSource({
            ".png, image/png",
            ".jpeg, image/jpeg",
            ".jpg, image/jpeg",
            ".gif, image/gif",
            ".svg, image/svg+xml"
        })
        public void shouldGetUserAvatar(String extension, String expectedMediaType) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders putHeaders = new HttpHeaders();
            putHeaders.setBearerAuth(auth.getAccessToken());
            putHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> putRequest = new HttpEntity<>(body, putHeaders);

            // GIVEN: Updated avatar
            testRestTemplate.exchange(
                url + "/users/me/avatar", HttpMethod.PUT, putRequest, new ParameterizedTypeReference<String>() {}
            );

            // GIVEN: Admin authentication header
            HttpHeaders getHeaders = new HttpHeaders();
            getHeaders.setBearerAuth(adminAuth.getAccessToken());

            // GIVEN: New user id in path
            String username = register.getUsername();
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Get avatar
            HttpEntity<Void> getRequest = new HttpEntity<>(null, getHeaders);
            ResponseEntity<Resource> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, getRequest, new ParameterizedTypeReference<Resource>() {}
            );

            // THEN: Returns avatar photo
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertArrayEquals(content, responseEntity.getBody().getContentAsByteArray()),
                () -> assertEquals(
                    MediaType.parseMediaType(expectedMediaType),
                    responseEntity.getHeaders().getContentType()
                )
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: User authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());

            // GIVEN: New user id in path
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Get avatar
            HttpEntity<Void> request = new HttpEntity<>(null, headers);
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
        public void should404_whenAvatarNonexistent() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            // GIVEN: New user id in path
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Get avatar
            HttpEntity<Void> request = new HttpEntity<>(null, headers);
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User '" + username + "' avatar not found.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should404_whenUserNonexistent() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            // GIVEN: Wrong user id in path
            User user = userService.get(username);
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Get avatar
            HttpEntity<Void> request = new HttpEntity<>(null, headers);
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
     * {@link UserAvatarController#updateUserAvatar} test.
     */
    @Nested
    public class UpdateUserAvatar {
        private static final String ENDPOINT = "/users/{userId}/avatar";

        @ParameterizedTest
        @CsvSource({".png", ".jpeg", ".jpg", ".gif", ".svg"})
        public void shouldUpdateUserAvatar(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            authenticationController.register(register);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // GIVEN: New user id in path
            String username = register.getUsername();
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user avatar
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Returns nothing and file is in storage
            user = userService.get(username);
            S3Object avatarObject = user.getAvatar();
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertArrayEquals(content, s3ObjectService.download(avatarObject).readAllBytes())
            );
        }

        @ParameterizedTest
        @CsvSource({".png", ".jpeg", ".jpg", ".gif", ".svg"})
        public void shouldReplaceOldAvatar(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            authenticationController.register(register);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Old avatar photo
            byte[] oldContent = "old-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(oldContent) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // GIVEN: New user id in path
            String username = register.getUsername();
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // GIVEN: Set old avatar photo
            testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<String>() {}
            );
            user = userService.get(username);
            final S3Object oldAvatarObject = user.getAvatar();

            // GIVEN: New avatar photo
            byte[] newContent = "new-image-content".getBytes(StandardCharsets.UTF_8);
            avatar = new ByteArrayResource(newContent) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            request = new HttpEntity<>(body, headers);

            // WHEN: Update user avatar
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Returns nothing and file is in storage
            user = userService.get(username);
            S3Object newAvatarObject = user.getAvatar();
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertArrayEquals(newContent, s3ObjectService.download(newAvatarObject).readAllBytes()),
                () -> assertThrows(StorageException.class, () -> s3ObjectService.download(oldAvatarObject))
            );
        }

        @ParameterizedTest
        @CsvSource({".csv", ".txt", ".xml", "''"})
        public void should400_whenMediaTypeInvalid(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            authenticationController.register(register);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // GIVEN: New user id in path
            String username = register.getUsername();
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user avatar
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals(
                    "Only PNG, JPEG, SVG and GIF images are allowed.", responseEntity.getBody().getMessage()
                )
            );
        }

        @ParameterizedTest
        @CsvSource({".png", ".jpeg", ".jpg", ".gif", ".svg"})
        public void should400_whenFileNull(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            authenticationController.register(register);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] content = "".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // GIVEN: New user id in path
            String username = register.getUsername();
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user avatar
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals(
                    "File must not be empty.", responseEntity.getBody().getMessage()
                )
            );
        }

        @ParameterizedTest
        @CsvSource({".png", ".jpeg", ".jpg", ".gif", ".svg"})
        public void should400_whenTooLarge(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            authenticationController.register(register);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            int size = (int) (maxAvatarSize + 1);
            byte[] content = new byte[size];
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // GIVEN: New user id in path
            String username = register.getUsername();
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user avatar
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals(
                    "File size exceeds limit (" + maxAvatarSize + " bytes).", responseEntity.getBody().getMessage()
                )
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: User authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar.png";
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // GIVEN: New user id in path
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Update user avatar
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
        public void should404_whenUserNonexistent() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            authenticationController.register(register);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar.png";
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // GIVEN: Wrong user id in path
            String username = register.getUsername();
            User user = userService.get(username);
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Update user avatar
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
    }

    /**
     * {@link UserAvatarController#deleteUserAvatar} test.
     */
    @Nested
    public class DeleteUserAvatar {
        private static final String ENDPOINT = "/users/{userId}/avatar";

        @ParameterizedTest
        @CsvSource({".png", ".jpeg", ".jpg", ".gif", ".svg"})
        public void shouldGetUserAvatar(String extension) {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: JWT authentication
            HttpHeaders putHeaders = new HttpHeaders();
            putHeaders.setBearerAuth(auth.getAccessToken());
            putHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] content = "fake-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource avatar = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatar);
            HttpEntity<MultiValueMap<String, Object>> putRequest = new HttpEntity<>(body, putHeaders);

            // GIVEN: Updated avatar
            testRestTemplate.exchange(
                url + "/users/me/avatar", HttpMethod.PUT, putRequest, new ParameterizedTypeReference<String>() {}
            );
            String username = register.getUsername();
            User user = userService.get(username);
            S3Object avatarObject = user.getAvatar();

            // GIVEN: Admin authentication header
            HttpHeaders getHeaders = new HttpHeaders();
            getHeaders.setBearerAuth(adminAuth.getAccessToken());

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Delete avatar
            HttpEntity<Void> getRequest = new HttpEntity<>(null, getHeaders);
            ResponseEntity<Resource> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, getRequest, new ParameterizedTypeReference<Resource>() {}
            );

            // THEN: Deletes avatar photo
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertThrows(StorageException.class, () -> s3ObjectService.download(avatarObject))
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeBuilder(RegisterDto.class).sample();
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register);

            // GIVEN: User authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());

            // GIVEN: New user id in path
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Delete avatar
            HttpEntity<Void> request = new HttpEntity<>(null, headers);
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
        public void should404_whenAvatarNonexistent() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            // GIVEN: New user id in path
            User user = userService.get(username);
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Delete avatar
            HttpEntity<Void> request = new HttpEntity<>(null, headers);
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User '" + username + "' avatar not found.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should404_whenUserNonexistent() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            authenticationController.register(register);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            // GIVEN: Wrong user id in path
            User user = userService.get(username);
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Delete avatar
            HttpEntity<Void> request = new HttpEntity<>(null, headers);
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
    }
}
