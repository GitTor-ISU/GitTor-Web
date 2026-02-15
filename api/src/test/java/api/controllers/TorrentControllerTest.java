package api.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import api.BasicContext;
import api.dtos.AuthenticationDto;
import api.dtos.ErrorDto;
import api.dtos.RegisterDto;
import api.dtos.TorrentDto;
import api.entities.User;
import api.services.AuthenticationService;

/**
 * {@link TorrentController} test.
 */
public class TorrentControllerTest extends BasicContext {
    @Autowired
    private AuthenticationController authenticationController;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private TorrentController torrentController;

    @Value("${pagination.default-page-size:10}")
    private int defaultPageSize;
    @Value("${pagination.max-page-size:100}")
    private int maxPageSize;

    private static final String TORRENT_MIME_TYPE = "application/x-bittorrent";

    /**
     * {@link TorrentController#uploadTorrent} test.
     */
    @Nested
    public class UploadTorrent {
        private static final String ENDPOINT = "/torrents";

        @Test
        public void shouldUploadTorrent() throws IOException {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register).getBody();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Torrent metadata
            TorrentDto metadata = fixtureMonkey.giveMeOne(TorrentDto.class);

            // GIVEN: Torrent file
            byte[] torrentContent = "fake-torrent-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource torrentFile = new ByteArrayResource(torrentContent) {
                @Override
                public String getFilename() {
                    return "test.torrent";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadata);
            body.add("file", torrentFile);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Upload torrent
            ResponseEntity<TorrentDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<TorrentDto>() {});

            // THEN: Returns torrent DTO
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertNotNull(responseEntity.getBody().getId()),
                () -> assertEquals(metadata.getName(), responseEntity.getBody().getName()),
                () -> assertEquals(metadata.getDescription(), responseEntity.getBody().getDescription()),
                () -> assertNotNull(responseEntity.getBody().getFileSize()),
                () -> assertNotNull(responseEntity.getBody().getUploaderId()),
                () -> assertNotNull(responseEntity.getBody().getCreatedAt()));
        }

        @Test
        public void should400_whenFileEmpty() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register).getBody();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Torrent metadata with empty file
            TorrentDto metadata = fixtureMonkey.giveMeOne(TorrentDto.class);
            var file = new ByteArrayResource(new byte[0]) {
                @Override
                public String getFilename() {
                    return "test.torrent";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadata);
            body.add("file", file);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Upload torrent
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds bad request
            assertAll(() -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()));
        }

        @Test
        public void should400_whenMetadataNull() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register).getBody();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Torrent file without metadata
            var metadata = new TorrentDto();
            byte[] torrentContent = "fake-torrent-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource torrentFile = new ByteArrayResource(torrentContent) {
                @Override
                public String getFilename() {
                    return "test.torrent";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadata);
            body.add("file", torrentFile);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Upload torrent
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds bad request
            assertAll(() -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()));
        }

        @Test
        public void should400_whenNameEmpty() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            AuthenticationDto auth = authenticationController.register(register).getBody();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // GIVEN: Torrent metadata with empty name
            TorrentDto metadata = TorrentDto.builder().name("").description("Test description").build();

            // GIVEN: Torrent file
            byte[] torrentContent = "fake-torrent-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource torrentFile = new ByteArrayResource(torrentContent) {
                @Override
                public String getFilename() {
                    return "test.torrent";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadata);
            body.add("file", torrentFile);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Upload torrent
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.POST,
                request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds bad request
            assertAll(() -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()));
        }
    }

    /**
     * {@link TorrentController#getAllTorrents} test.
     */
    @Nested
    public class GetAllTorrents {
        private static final String ENDPOINT = "/torrents";

        @Test
        public void shouldGetAllTorrents() {
            // GIVEN: Multiple torrents uploaded
            for (int i = 0; i < 5; i++) {
                uploadTestTorrent();
            }

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Get all torrents
            ResponseEntity<List<TorrentDto>> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.GET,
                request, new ParameterizedTypeReference<List<TorrentDto>>() {});

            // THEN: Returns paginated list of torrents
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertTrue(responseEntity.getBody().size() >= 5,
                    "Should get at least 5 torrents: found " + responseEntity.getBody().size()));
        }

        @Test
        public void shouldGetAllTorrents_whenQueryParameters() {
            // GIVEN: Multiple torrents uploaded
            for (int i = 0; i < 15; i++) {
                uploadTestTorrent();
            }

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Get all torrents with pagination
            String urlWithParams = url + ENDPOINT + "?page=1&size=5";
            ResponseEntity<List<TorrentDto>> responseEntity = testRestTemplate.exchange(urlWithParams, HttpMethod.GET,
                request, new ParameterizedTypeReference<List<TorrentDto>>() {});

            // THEN: Returns requested page
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertEquals(5, responseEntity.getBody().size()));
        }

        @Test
        public void shouldGetAllTorrents_whenQueryParametersExceedLimit() {
            // GIVEN: Many torrents uploaded
            for (int i = 0; i < 150; i++) {
                uploadTestTorrent();
            }

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Get all torrents with size exceeding max
            String urlWithParams = url + ENDPOINT + "?page=0&size=200";
            ResponseEntity<List<TorrentDto>> responseEntity = testRestTemplate.exchange(urlWithParams, HttpMethod.GET,
                request, new ParameterizedTypeReference<List<TorrentDto>>() {});

            // THEN: Returns capped at max page size
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(maxPageSize, responseEntity.getBody().size()));
        }
    }

    /**
     * {@link TorrentController#getTorrent} test.
     */
    @Nested
    public class GetTorrent {
        private static final String ENDPOINT = "/torrents/{id}";

        @Test
        public void shouldGetTorrent() {
            // GIVEN: Torrent uploaded
            TorrentDto uploaded = uploadTestTorrent();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Get torrent by id
            ResponseEntity<TorrentDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.GET,
                request, new ParameterizedTypeReference<TorrentDto>() {}, uploaded.getId());

            // THEN: Returns torrent
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(uploaded.getId(), responseEntity.getBody().getId()),
                () -> assertEquals(uploaded.getName(), responseEntity.getBody().getName()),
                () -> assertEquals(uploaded.getDescription(), responseEntity.getBody().getDescription()));
        }

        @Test
        public void should404_whenNonexistent() {
            // GIVEN: Nonexistent torrent id
            Long nonexistentId = 999999L;

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Get torrent by id
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.GET, request,
                new ParameterizedTypeReference<ErrorDto>() {}, nonexistentId);

            // THEN: Responds not found
            assertAll(() -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()));
        }
    }

    /**
     * {@link TorrentController#updateTorrent} test.
     */
    @Nested
    public class UpdateTorrent {
        private static final String ENDPOINT = "/torrents/{id}";

        @Test
        public void shouldUpdateTorrent_whenNameChanged() {
            // GIVEN: Torrent uploaded
            TorrentDto uploaded = uploadTestTorrent();

            // GIVEN: Update metadata with new name
            TorrentDto updateDto = TorrentDto.builder().name("New Name").description(uploaded.getDescription()).build();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", updateDto);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update torrent
            ResponseEntity<TorrentDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.PUT,
                request, new ParameterizedTypeReference<TorrentDto>() {}, uploaded.getId());

            // THEN: Returns updated torrent
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(uploaded.getId(), responseEntity.getBody().getId()),
                () -> assertEquals("New Name", responseEntity.getBody().getName()),
                () -> assertEquals(uploaded.getDescription(), responseEntity.getBody().getDescription()));
        }

        @Test
        public void shouldUpdateTorrent_whenDescriptionChanged() {
            // GIVEN: Torrent uploaded
            TorrentDto uploaded = uploadTestTorrent();

            // GIVEN: Update metadata with new description
            TorrentDto updateDto = TorrentDto.builder().name(uploaded.getName()).description("New Description").build();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", updateDto);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update torrent
            ResponseEntity<TorrentDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.PUT,
                request, new ParameterizedTypeReference<TorrentDto>() {}, uploaded.getId());

            // THEN: Returns updated torrent
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(uploaded.getId(), responseEntity.getBody().getId()),
                () -> assertEquals(uploaded.getName(), responseEntity.getBody().getName()),
                () -> assertEquals("New Description", responseEntity.getBody().getDescription()));
        }

        @Test
        public void shouldUpdateTorrent_whenBothChanged() {
            // GIVEN: Torrent uploaded
            final TorrentDto uploaded = uploadTestTorrent();

            // GIVEN: Update metadata with new name and description
            TorrentDto updateDto = TorrentDto.builder().name("New Name").description("New Description").build();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", updateDto);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update torrent
            ResponseEntity<TorrentDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.PUT,
                request, new ParameterizedTypeReference<TorrentDto>() {}, uploaded.getId());

            // THEN: Returns updated torrent
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(uploaded.getId(), responseEntity.getBody().getId()),
                () -> assertEquals("New Name", responseEntity.getBody().getName()),
                () -> assertEquals("New Description", responseEntity.getBody().getDescription()));
        }

        @Test
        public void should404_whenNonexistent() {
            // GIVEN: Nonexistent torrent id
            final Long nonexistentId = 999999L;

            // GIVEN: Update metadata
            TorrentDto updateDto = TorrentDto.builder().name("New Name").description("New Description").build();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", updateDto);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update torrent
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.PUT, request,
                new ParameterizedTypeReference<ErrorDto>() {}, nonexistentId);

            // THEN: Responds not found
            assertAll(() -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()));
        }
    }

    /**
     * {@link TorrentController#deleteTorrent} test.
     */
    @Nested
    public class DeleteTorrent {
        private static final String ENDPOINT = "/torrents/{id}";

        @Test
        public void shouldDeleteTorrent() {
            // GIVEN: Torrent uploaded
            TorrentDto uploaded = uploadTestTorrent();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Delete torrent
            ResponseEntity<Void> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.DELETE, request,
                new ParameterizedTypeReference<Void>() {}, uploaded.getId());

            // THEN: Responds OK
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            // THEN: Torrent no longer exists
            ResponseEntity<ErrorDto> getResponse = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.GET, request,
                new ParameterizedTypeReference<ErrorDto>() {}, uploaded.getId());
            assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        }

        @Test
        public void should404_whenNonexistent() {
            // GIVEN: Nonexistent torrent id
            Long nonexistentId = 999999L;

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Delete torrent
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.DELETE,
                request, new ParameterizedTypeReference<ErrorDto>() {}, nonexistentId);

            // THEN: Responds not found
            assertAll(() -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()));
        }
    }

    /**
     * {@link TorrentController#getTorrentFile} test.
     */
    @Nested
    public class GetTorrentFile {
        private static final String ENDPOINT = "/torrents/{id}/file";

        @Test
        public void shouldGetTorrentFile() throws IOException {
            // GIVEN: Torrent uploaded with known content
            byte[] expectedContent = "fake-torrent-content".getBytes(StandardCharsets.UTF_8);
            TorrentDto uploaded = uploadTestTorrent(expectedContent);

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Download torrent file
            ResponseEntity<Resource> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.GET, request,
                new ParameterizedTypeReference<Resource>() {}, uploaded.getId());

            // THEN: Returns file with correct content
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertArrayEquals(expectedContent, responseEntity.getBody().getContentAsByteArray()));
        }

        @Test
        public void shouldGetTorrentFile_withCorrectMediaType() {
            // GIVEN: Torrent uploaded
            TorrentDto uploaded = uploadTestTorrent();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Download torrent file
            ResponseEntity<Resource> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.GET, request,
                new ParameterizedTypeReference<Resource>() {}, uploaded.getId());

            // THEN: Returns correct media type
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getHeaders().getContentType()),
                () -> assertEquals(MediaType.parseMediaType(TORRENT_MIME_TYPE),
                    responseEntity.getHeaders().getContentType()));
        }

        @Test
        public void shouldGetTorrentFile_withContentDisposition() {
            // GIVEN: Torrent uploaded
            TorrentDto uploaded = uploadTestTorrent();

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Download torrent file
            ResponseEntity<Resource> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.GET, request,
                new ParameterizedTypeReference<Resource>() {}, uploaded.getId());

            // THEN: Returns content disposition header
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getHeaders().getContentDisposition()),
                () -> assertTrue(responseEntity.getHeaders().getContentDisposition().toString().contains("attachment")),
                () -> assertTrue(responseEntity.getHeaders().getContentDisposition().toString().contains(".torrent")));
        }

        @Test
        public void should404_whenNonexistent() {
            // GIVEN: Nonexistent torrent id
            Long nonexistentId = 999999L;

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());

            var request = new HttpEntity<>(headers);

            // WHEN: Download torrent file
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.GET, request,
                new ParameterizedTypeReference<ErrorDto>() {}, nonexistentId);

            // THEN: Responds not found
            assertAll(() -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()));
        }
    }

    /**
     * {@link TorrentController#updateTorrentFile} test.
     */
    @Nested
    public class UpdateTorrentFile {
        private static final String ENDPOINT = "/torrents/{id}/file";

        @Test
        public void shouldUpdateTorrentFile() {
            // GIVEN: Torrent uploaded
            final TorrentDto uploaded = uploadTestTorrent();

            // GIVEN: New torrent file
            byte[] newContent = "new-torrent-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource newFile = new ByteArrayResource(newContent) {
                @Override
                public String getFilename() {
                    return "updated.torrent";
                }
            };

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", newFile);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update torrent file
            ResponseEntity<Void> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.PUT, request,
                new ParameterizedTypeReference<Void>() {}, uploaded.getId());

            // THEN: Responds OK
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        }

        @Test
        public void should400_whenFileEmpty() {
            // GIVEN: Torrent uploaded
            final TorrentDto uploaded = uploadTestTorrent();

            // GIVEN: JWT authentication without file
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            var file = new ByteArrayResource(new byte[0]) {
                @Override
                public String getFilename() {
                    return "test.torrent";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update torrent file
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.PUT, request,
                new ParameterizedTypeReference<ErrorDto>() {}, uploaded.getId());

            // THEN: Responds bad request
            assertAll(() -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()));
        }

        @Test
        public void should404_whenNonexistent() {
            // GIVEN: Nonexistent torrent id
            final Long nonexistentId = 999999L;

            // GIVEN: New torrent file
            byte[] newContent = "new-torrent-content".getBytes(StandardCharsets.UTF_8);
            ByteArrayResource newFile = new ByteArrayResource(newContent) {
                @Override
                public String getFilename() {
                    return "updated.torrent";
                }
            };

            // GIVEN: JWT authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", newFile);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // WHEN: Update torrent file
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(url + ENDPOINT, HttpMethod.PUT, request,
                new ParameterizedTypeReference<ErrorDto>() {}, nonexistentId);

            // THEN: Responds not found
            assertAll(() -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()));
        }
    }

    /**
     * Helper method to upload a test torrent.
     *
     * @return Uploaded torrent DTO
     */
    private TorrentDto uploadTestTorrent() {
        byte[] defaultContent = "fake-torrent-content".getBytes(StandardCharsets.UTF_8);
        return uploadTestTorrent(defaultContent);
    }

    /**
     * Helper method to upload a test torrent with specific content.
     *
     * @param content Torrent file content
     * @return Uploaded torrent DTO
     */
    private TorrentDto uploadTestTorrent(byte[] content) {
        RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
        User user = authenticationService.register(register);

        TorrentDto metadata = fixtureMonkey.giveMeOne(TorrentDto.class);
        MockMultipartFile file = new MockMultipartFile("file", "test.torrent", TORRENT_MIME_TYPE, content);

        try {
            return torrentController.uploadTorrent(user, metadata, file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload test torrent", e);
        }
    }
}
