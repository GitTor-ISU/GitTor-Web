package api.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
import api.dtos.ErrorDto;
import api.dtos.RegisterDto;
import api.dtos.RoleDto;
import api.entities.Authority;
import api.entities.Role;
import api.mapper.RoleMapper;
import api.services.AuthorityService;
import api.services.RoleService;

/**
 * {@link RoleController} test.
 */
public class RoleControllerTest extends BasicControllerTest {
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private AuthenticationController authenticationController;
    @Autowired
    private RoleMapper roleMapper;

    /**
     * {@link RoleController#getRoles} test.
     */
    @Nested
    public class GetRoles {
        private static final String ENDPOINT = "/roles";

        @Test
        public void shouldGetRoles() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists with new authority
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).authorities(Set.of(authority)).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // WHEN: Get roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.GET, request, new ParameterizedTypeReference<List<RoleDto>>() {}
            );

            // THEN: New role should be in list
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertTrue(
                    responseEntity.getBody().stream().anyMatch(a -> roleName.equals(a.getName())),
                    "Expected role '" + roleName + "' not found"
                ),
                () -> assertTrue(
                    responseEntity.getBody().stream()
                        .anyMatch(a -> {
                            return role.getId() == a.getId()
                                && a.getAuthorityIds().size() == 1
                                && a.getAuthorityIds().contains(authority.getId());
                        }),
                    "Expected authority '" + authorityName + "' not found in role '" + roleName + "'"
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

            // WHEN: Get roles
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
     * {@link RoleController#getRole} test.
     */
    @Nested
    public class GetRole {
        private static final String ENDPOINT = "/roles/{id}";

        @Test
        public void shouldGetRole() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists with new authority
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).authorities(Set.of(authority)).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Get role
            ResponseEntity<RoleDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<RoleDto>() {}
            );

            // THEN: New role should be returned
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(roleMapper.toDto(role), responseEntity.getBody()),
                () -> assertEquals(1, responseEntity.getBody().getAuthorityIds().size()),
                () -> assertTrue(
                    responseEntity.getBody().getAuthorityIds().contains(authority.getId()),
                    "Expected authority '" + authorityName + "' not found in role '" + roleName + "'"
                )
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New role exists
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).build());

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
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Get role
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
            // GIVEN: New role exists
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Wrong role id
            int wrongId = role.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Get role
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Role " + wrongId + " not found.", responseEntity.getBody().getMessage())
            );
        }
    }

    /**
     * {@link RoleController#createRole} test.
     */
    @Nested
    public class CreateRole {
        private static final String ENDPOINT = "/roles";

        @Test
        public void shouldCreateRole() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: Role with new authority id
            String roleName = "role_" + UUID.randomUUID();
            RoleDto role = RoleDto.builder()
                .name(roleName)
                .authorityIds(List.of(authority.getId()))
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(role, headers);

            // WHEN: Create role
            ResponseEntity<RoleDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<RoleDto>() {}
            );

            // THEN: New role should be created
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(roleName, responseEntity.getBody().getName()),
                () -> assertEquals(List.of(authority.getId()), responseEntity.getBody().getAuthorityIds())
            );
        }

        @Test
        public void shouldCreateWithoutAuthority_whenAuthorityNonexistent() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: Role with wrong authority id
            int wrongId = authority.getId() + 1;
            String roleName = "role_" + UUID.randomUUID();
            RoleDto role = RoleDto.builder()
                .name(roleName)
                .authorityIds(List.of(wrongId))
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(role, headers);

            // WHEN: Create role
            ResponseEntity<RoleDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<RoleDto>() {}
            );

            // THEN: New role should be created without authorities
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(roleName, responseEntity.getBody().getName()),
                () -> assertEquals(Collections.emptyList(), responseEntity.getBody().getAuthorityIds())
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: Role
            String roleName = "role_" + UUID.randomUUID();
            RoleDto role = RoleDto.builder()
                .name(roleName)
                .build();

            // GIVEN: User authentication header
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username("user_" + UUID.randomUUID())
                    .password("password")
                    .build()
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(role, headers);

            // WHEN: Create role
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<ErrorDto>() {}
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
        public void should400_whenNameEmpty() {
            // GIVEN: Role with empty name
            RoleDto role = RoleDto.builder()
                .name("")
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(role, headers);

            // WHEN: Create role
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Role name must not be empty.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should409_whenDuplicateRoleName() {
            // GIVEN: Role exists
            String roleName = "role_" + UUID.randomUUID();
            roleService.save(Role.builder().name(roleName).build());

            // GIVEN: Role with duplicate name
            RoleDto role = RoleDto.builder()
                .name(roleName)
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(role, headers);

            // WHEN: Create role
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                url + ENDPOINT, HttpMethod.POST, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds conflict
            assertAll(
                () -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Role '" + roleName + "' already exists.", responseEntity.getBody().getMessage())
            );
        }
    }

    /**
     * {@link RoleController#updateRole} test.
     */
    @Nested
    public class UpdateRole {
        private static final String ENDPOINT = "/roles/{id}";

        @Test
        public void shouldUpdateRole_whenUpdateName() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists with new authority
            String oldRoleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(oldRoleName).authorities(Set.of(authority)).build());

            // GIVEN: Updated name, but some values null (id, authorities)
            String roleName = "role_" + UUID.randomUUID();
            RoleDto updated = RoleDto.builder()
                .name(roleName)
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(updated, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Update role
            ResponseEntity<RoleDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<RoleDto>() {}
            );

            // THEN: Updated role should be returned
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(roleName, responseEntity.getBody().getName()),
                () -> assertEquals(1, responseEntity.getBody().getAuthorityIds().size()),
                () -> assertTrue(
                    responseEntity.getBody().getAuthorityIds().contains(authority.getId()),
                    "Expected authority '" + authorityName + "' not found in role '" + oldRoleName + "'"
                )
            );
        }

        @Test
        public void shouldUpdateRole_whenUpdateAuthorities() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).build());

            // GIVEN: Updated authorities with new authority
            RoleDto updated = RoleDto.builder()
                .authorityIds(List.of(authority.getId()))
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(updated, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Update role
            ResponseEntity<RoleDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<RoleDto>() {}
            );

            // THEN: Updated role should be returned
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(roleName, responseEntity.getBody().getName()),
                () -> assertEquals(1, responseEntity.getBody().getAuthorityIds().size()),
                () -> assertTrue(
                    responseEntity.getBody().getAuthorityIds().contains(authority.getId()),
                    "Expected authority '" + authorityName + "' not found in role '" + roleName + "'"
                )
            );
        }

        @Test
        public void shouldUpdateRole_whenBodyIdIncorrect() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists with new authority
            String oldRoleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(oldRoleName).authorities(Set.of(authority)).build());

            // GIVEN: Updated name, but some values null (id, authorities)
            String roleName = "role_" + UUID.randomUUID();
            RoleDto updated = RoleDto.builder()
                .id(role.getId() + 1)
                .name(roleName)
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(updated, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Update role
            ResponseEntity<RoleDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<RoleDto>() {}
            );

            // THEN: Updated role should be returned
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(roleName, responseEntity.getBody().getName()),
                () -> assertEquals(1, responseEntity.getBody().getAuthorityIds().size()),
                () -> assertTrue(
                    responseEntity.getBody().getAuthorityIds().contains(authority.getId()),
                    "Expected authority '" + authorityName + "' not found in role '" + oldRoleName + "'"
                )
            );
        }

        @Test
        public void shouldUpdateRole_whenNameNotChanged() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).build());

            // GIVEN: Updated authorities with new authority and insert same name
            RoleDto updated = RoleDto.builder()
                .name(roleName)
                .authorityIds(List.of(authority.getId()))
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(updated, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Update role
            ResponseEntity<RoleDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<RoleDto>() {}
            );

            // THEN: Updated role should be returned
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(roleName, responseEntity.getBody().getName()),
                () -> assertEquals(1, responseEntity.getBody().getAuthorityIds().size()),
                () -> assertTrue(
                    responseEntity.getBody().getAuthorityIds().contains(authority.getId()),
                    "Expected authority '" + authorityName + "' not found in role '" + roleName + "'"
                )
            );
        }

        @Test
        public void shouldUpdateWithoutAuthority_whenAuthorityNonexistent() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).build());

            // GIVEN: Updated authorities with wrong id
            int wrongId = authority.getId() + 1;
            RoleDto updated = RoleDto.builder()
                .authorityIds(List.of(wrongId))
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(updated, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Update role
            ResponseEntity<RoleDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<RoleDto>() {}
            );

            // THEN: Updated role should be returned
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(roleName, responseEntity.getBody().getName()),
                () -> assertEquals(0, responseEntity.getBody().getAuthorityIds().size())
            );
        }

        @Test
        public void should400_whenNameEmpty() {
            // GIVEN: New role exists
            String oldRoleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(oldRoleName).build());

            // GIVEN: Updated name to empty
            RoleDto updated = RoleDto.builder()
                .name("")
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(updated, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Update role
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds bad request
            assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Role name must not be empty.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New role exists
            String oldRoleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(oldRoleName).build());

            // GIVEN: Updated name
            String roleName = "role_" + UUID.randomUUID();
            RoleDto updated = RoleDto.builder()
                .name(roleName)
                .build();

            // GIVEN: User authentication header
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username("user_" + UUID.randomUUID())
                    .password("password")
                    .build()
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(updated, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Update role
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
        public void should404_whenRoleNonexistent() {
            // GIVEN: New role exists
            String oldRoleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(oldRoleName).build());

            // GIVEN: Updated name
            String roleName = "role_" + UUID.randomUUID();
            RoleDto updated = RoleDto.builder()
                .name(roleName)
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(updated, headers);

            // GIVEN: Wrong role id
            int wrongId = role.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Update role
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Role " + wrongId + " not found.", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should409_whenDuplicateName() {
            // GIVEN: Two new role exists
            String oldRoleName1 = "role_" + UUID.randomUUID();
            String roleName2 = "role_" + UUID.randomUUID();
            Role role1 = roleService.save(Role.builder().name(oldRoleName1).build());
            roleService.save(Role.builder().name(roleName2).build());

            // GIVEN: Updated name to other role name
            RoleDto updated = RoleDto.builder()
                .name(roleName2)
                .build();

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<RoleDto> request = new HttpEntity<>(updated, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(role1.getId())
                .toUri();

            // WHEN: Update role
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds conflict
            assertAll(
                () -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Role '" + roleName2 + "' already exists.", responseEntity.getBody().getMessage())
            );
        }
    }

    /**
     * {@link RoleController#deleteRole} test.
     */
    @Nested
    public class DeleteRole {
        private static final String ENDPOINT = "/roles/{id}";

        @Test
        public void shouldDeleteRole() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists with new authority
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).authorities(Set.of(authority)).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Delete role
            ResponseEntity<String> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, request, new ParameterizedTypeReference<String>() {}
            );

            // THEN: Returns nothing and role doesn't exist
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNull(responseEntity.getBody()),
                () -> assertFalse(
                    roleService.exists(roleName),
                    "Unexpected user '" + roleName + "' found"
                )
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New role exists
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).build());

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
                .buildAndExpand(role.getId())
                .toUri();

            // WHEN: Delete role
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
            // GIVEN: New role exists
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Wrong role id
            int wrongId = role.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Delete role
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Responds not found
            assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Role " + wrongId + " not found.", responseEntity.getBody().getMessage())
            );
        }
    }
}
