package api.controllers.users;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
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

import api.controllers.AuthenticationController;
import api.controllers.BasicControllerTest;
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
public class UserAvatarControllerTest extends BasicControllerTest {
    @Autowired
    private AuthenticationController authenticationController;
    @Autowired
    private UserService userService;
    @Autowired
    private S3ObjectService s3ObjectService;

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
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );

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
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody())
            );
        }

        @ParameterizedTest
        @CsvSource({".csv", ".txt", ".xml"})
        public void should400_whenMediaTypeInvalid(String extension) {
            // GIVEN: New user registered
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );

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
    }

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
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );

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
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );

            // GIVEN: JWT authentication
            HttpHeaders putHeaders = new HttpHeaders();
            putHeaders.setBearerAuth(auth.getAccessToken());
            putHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Avatar photo
            byte[] oldContent = "old-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource oldAvatar = new ByteArrayResource(oldContent) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", oldAvatar);
            HttpEntity<MultiValueMap<String, Object>> putRequest = new HttpEntity<>(body, putHeaders);

            // GIVEN: Updated avatar to old photo
            testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.PUT, putRequest, new ParameterizedTypeReference<String>() {}
            );

            // GIVEN: Update avatar photo
            byte[] newContent = "new-image-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource newAvatar = new ByteArrayResource(newContent) {
                @Override
                public String getFilename() {
                    return "avatar" + extension;
                }
            };
            body = new LinkedMultiValueMap<>();
            body.add("file", newAvatar);
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
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );

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
     * {@link UserAvatarController#deleteMyAvatar} test.
     */
    @Nested
    public class DeleteMyAvatar {
        private static final String ENDPOINT = "/users/me/avatar";

        @ParameterizedTest
        @CsvSource({".png", ".jpeg", ".jpg", ".gif", ".svg"})
        public void shouldDeleteMyAvatar(String extension) {
            // GIVEN: New user registered
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );

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
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );

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
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );

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
}
