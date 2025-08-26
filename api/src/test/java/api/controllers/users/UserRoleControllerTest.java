package api.controllers.users;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import api.controllers.AuthenticationController;
import api.controllers.BasicControllerTest;
import api.dtos.AuthenticationDto;
import api.dtos.ErrorDto;
import api.dtos.RegisterDto;
import api.dtos.RoleDto;
import api.entities.Authority;
import api.entities.Role;
import api.entities.User;
import api.services.AuthorityService;
import api.services.RoleService;
import api.services.UserService;

/**
 * {@link UserRoleController} test.
 */
public class UserRoleControllerTest extends BasicControllerTest {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private AuthenticationController authenticationController;

    /**
     * {@link UserRoleController#getUserRoles} test.
     */
    @Nested
    public class GetUserRoles {
        private static final String ENDPOINT = "/users/{id}/roles";

        @Test
        public void shouldGetUserRoles() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists with new authority
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).authorities(Set.of(authority)).build());

            // GIVEN: New user exists with new role
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).roles(Set.of(role)).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Get user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<List<RoleDto>>() {}
            );

            // THEN: Returns user roles
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(1, responseEntity.getBody().size()),
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
            // GIVEN: New user registered
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );
            User user = userService.get(username);

            // GIVEN: User authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Get user roles
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Returns users
            assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should404_whenNonexistentUser() {
            // GIVEN: New user exists
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).build());

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

            // WHEN: Get user roles
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
     * {@link UserRoleController#setUserRoles} test.
     */
    @Nested
    public class SetUserRoles {
        private static final String ENDPOINT = "/users/{id}/roles";

        @Test
        public void shouldSetUserRoles() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: Two new role exists (second one with authorities)
            String roleName1 = "role_" + UUID.randomUUID();
            String roleName2 = "role_" + UUID.randomUUID();
            Role role1 = roleService.save(Role.builder().name(roleName1).build());
            Role role2 = roleService.save(Role.builder().name(roleName2).authorities(Set.of(authority)).build());

            // GIVEN: New user exists with new role
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).roles(Set.of(role1)).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role2.getId()), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Set user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.POST, request, new ParameterizedTypeReference<List<RoleDto>>() {}
            );

            // THEN: Returns user roles
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(1, responseEntity.getBody().size()),
                () -> assertTrue(
                    responseEntity.getBody().stream()
                        .anyMatch(a -> {
                            return role2.getId() == a.getId()
                                && a.getAuthorityIds().size() == 1
                                && a.getAuthorityIds().contains(authority.getId());
                        }),
                    "Expected authority '" + authorityName + "' not found in role '" + roleName2 + "'"
                )
            );
        }

        @Test
        public void shouldSetUserRoles_whenRolesNotFound() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists with new authority
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).authorities(Set.of(authority)).build());

            // GIVEN: New user exists with new role
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).roles(Set.of(role)).build());

            // GIVEN: Admin authentication header with wrong role id
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role.getId() + 1), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Set user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.POST, request, new ParameterizedTypeReference<List<RoleDto>>() {}
            );

            // THEN: Returns no user roles
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(0, responseEntity.getBody().size())
            );
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );
            User user = userService.get(username);

            // GIVEN: User authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Set user roles
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.POST, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Returns users
            assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should404_whenNonexistentUser() {
            // GIVEN: New user exists
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: Wrong user id in path
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Set user roles
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
     * {@link UserRoleController#addUserRoles} test.
     */
    @Nested
    public class AddUserRoles {
        private static final String ENDPOINT = "/users/{id}/roles";

        @Test
        public void shouldAddUserRoles() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: Two new role exists (second one with authorities)
            String roleName1 = "role_" + UUID.randomUUID();
            String roleName2 = "role_" + UUID.randomUUID();
            Role role1 = roleService.save(Role.builder().name(roleName1).build());
            Role role2 = roleService.save(Role.builder().name(roleName2).authorities(Set.of(authority)).build());

            // GIVEN: New user exists with new role
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).roles(Set.of(role1)).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role2.getId()), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Add user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<List<RoleDto>>() {}
            );

            // THEN: Returns user roles
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(2, responseEntity.getBody().size()),
                () -> assertTrue(
                    responseEntity.getBody().stream()
                        .anyMatch(a -> role1.getId() == a.getId()),
                    "Expected authority '" + authorityName + "' not found in role '" + roleName1 + "'"
                ),
                () -> assertTrue(
                    responseEntity.getBody().stream()
                        .anyMatch(a -> {
                            return role2.getId() == a.getId()
                                && a.getAuthorityIds().size() == 1
                                && a.getAuthorityIds().contains(authority.getId());
                        }),
                    "Expected authority '" + authorityName + "' not found in role '" + roleName2 + "'"
                )
            );
        }

        @Test
        public void shouldAddUserRoles_whenRolesNotFound() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists with new authority
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).authorities(Set.of(authority)).build());

            // GIVEN: New user exists with new role
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).roles(Set.of(role)).build());

            // GIVEN: Admin authentication header with wrong role id
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role.getId() + 1), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Add user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<List<RoleDto>>() {}
            );

            // THEN: Returns no user roles
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(1, responseEntity.getBody().size()),
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
        public void shouldNotAddUserRoles_whenRolesAlreadyAdded() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists with new authority
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).authorities(Set.of(authority)).build());

            // GIVEN: New user exists with new role
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).roles(Set.of(role)).build());

            // GIVEN: Admin authentication header with wrong role id
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role.getId()), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Add user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<List<RoleDto>>() {}
            );

            // THEN: Returns no user roles
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(1, responseEntity.getBody().size()),
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
            // GIVEN: New user registered
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );
            User user = userService.get(username);

            // GIVEN: User authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Add user roles
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Returns users
            assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should404_whenNonexistentUser() {
            // GIVEN: New user exists
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: Wrong user id in path
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Add user roles
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
     * {@link UserRoleController#removeUserRoles} test.
     */
    @Nested
    public class RemoveUserRoles {
        private static final String ENDPOINT = "/users/{id}/roles";

        @Test
        public void shouldRemoveUserRoles() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: Two new role exists (second one with authorities)
            String roleName1 = "role_" + UUID.randomUUID();
            String roleName2 = "role_" + UUID.randomUUID();
            Role role1 = roleService.save(Role.builder().name(roleName1).build());
            Role role2 = roleService.save(Role.builder().name(roleName2).authorities(Set.of(authority)).build());

            // GIVEN: New user exists with new role
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).roles(Set.of(role1, role2)).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role1.getId()), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Remove user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, request, new ParameterizedTypeReference<List<RoleDto>>() {}
            );

            // THEN: Returns user roles without deleted role
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(1, responseEntity.getBody().size()),
                () -> assertTrue(
                    responseEntity.getBody().stream()
                        .anyMatch(a -> {
                            return role2.getId() == a.getId()
                                && a.getAuthorityIds().size() == 1
                                && a.getAuthorityIds().contains(authority.getId());
                        }),
                    "Expected authority '" + authorityName + "' not found in role '" + roleName2 + "'"
                )
            );
        }

        @Test
        public void shouldNotRemoveUserRoles_whenRolesNotFound() {
            // GIVEN: New authority exists
            String authorityName = "authority_" + UUID.randomUUID();
            Authority authority = authorityService.save(Authority.builder().authority(authorityName).build());

            // GIVEN: New role exists with new authority
            String roleName = "role_" + UUID.randomUUID();
            Role role = roleService.save(Role.builder().name(roleName).authorities(Set.of(authority)).build());

            // GIVEN: New user exists with new role
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).roles(Set.of(role)).build());

            // GIVEN: Admin authentication header with wrong role id
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role.getId() + 1), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Remove user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, request, new ParameterizedTypeReference<List<RoleDto>>() {}
            );

            // THEN: Returns no user roles
            assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(1, responseEntity.getBody().size()),
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
            // GIVEN: New user registered
            String username = "user_" + UUID.randomUUID();
            AuthenticationDto auth = authenticationController.register(
                RegisterDto.builder()
                    .username(username)
                    .password("password")
                    .build()
            );
            User user = userService.get(username);

            // GIVEN: User authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(user.getId())
                .toUri();

            // WHEN: Remove user roles
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(
                uri, HttpMethod.DELETE, request, new ParameterizedTypeReference<ErrorDto>() {}
            );

            // THEN: Returns users
            assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage())
            );
        }

        @Test
        public void should404_whenNonexistentUser() {
            // GIVEN: New user exists
            String username = "user_" + UUID.randomUUID();
            User user = userService.save(User.builder().username(username).build());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: Wrong user id in path
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url)
                .path(ENDPOINT)
                .buildAndExpand(wrongId)
                .toUri();

            // WHEN: Remove user roles
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
