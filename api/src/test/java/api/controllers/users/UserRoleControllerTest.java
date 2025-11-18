package api.controllers.users;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

import api.BasicContext;
import api.controllers.AuthenticationController;
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
public class UserRoleControllerTest extends BasicContext {
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
        private final Role userRole = roleService.get(RoleService.USER_ROLE_NAME);

        @Test
        public void shouldGetUserRoles() {
            // GIVEN: New authority exists
            Authority authority = authorityService.save(fixtureMonkey.giveMeOne(Authority.class));
            String authorityName = authority.getAuthority();

            // GIVEN: New role exists with new authority
            Role role = roleService.save(fixtureMonkey.giveMeBuilder(Role.class)
                .set(javaGetter(Role::getAuthorities), Set.of(authority)).sample());
            String roleName = role.getName();

            // GIVEN: New user exists with new role
            User user = userService.save(fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getRoles), Set.of(role, userRole)).sample());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Get user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(uri, HttpMethod.GET, request,
                new ParameterizedTypeReference<List<RoleDto>>() {});

            // THEN: Returns user roles
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertEquals(2, responseEntity.getBody().size()),
                () -> assertTrue(responseEntity.getBody().stream().anyMatch(a -> {
                    return role.getId() == a.getId() && a.getAuthorityIds().size() == 1
                        && a.getAuthorityIds().contains(authority.getId());
                }), "Expected authority '" + authorityName + "' not found in role '" + roleName + "'"));
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register).getBody();
            User user = userService.get(username);

            // GIVEN: User authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Get user roles
            ResponseEntity<ErrorDto> responseEntity =
                testRestTemplate.exchange(uri, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Returns users
            assertAll(() -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage()));
        }

        @Test
        public void should404_whenNonexistentUser() {
            // GIVEN: New user exists
            User user = userService.save(
                fixtureMonkey.giveMeBuilder(User.class).set(javaGetter(User::getRoles), Set.of(userRole)).sample());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            // GIVEN: Wrong user id in path
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(wrongId).toUri();

            // WHEN: Get user roles
            ResponseEntity<ErrorDto> responseEntity =
                testRestTemplate.exchange(uri, HttpMethod.GET, request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds not found
            assertAll(() -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User " + wrongId + " not found.", responseEntity.getBody().getMessage()));
        }
    }

    /**
     * {@link UserRoleController#setUserRoles} test.
     */
    @Nested
    public class SetUserRoles {
        private static final String ENDPOINT = "/users/{id}/roles";
        private final Role userRole = roleService.get(RoleService.USER_ROLE_NAME);

        @Test
        public void shouldSetUserRoles() {
            // GIVEN: New authority exists
            Authority authority = authorityService.save(fixtureMonkey.giveMeOne(Authority.class));
            String authorityName = authority.getAuthority();

            // GIVEN: Two new role exists (second one with authorities)
            Role role1 = roleService.save(fixtureMonkey.giveMeOne(Role.class));
            Role role2 = roleService.save(fixtureMonkey.giveMeBuilder(Role.class)
                .set(javaGetter(Role::getAuthorities), Set.of(authority)).sample());
            String roleName2 = role2.getName();

            // GIVEN: New user exists with new role
            User user = userService.save(fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getRoles), Set.of(role1, userRole)).sample());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role2.getId(), userRole.getId()), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Set user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
                new ParameterizedTypeReference<List<RoleDto>>() {});

            // THEN: Returns user roles, including 'USER' role
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertEquals(2, responseEntity.getBody().size()),
                () -> assertTrue(responseEntity.getBody().stream().anyMatch(a -> {
                    return role2.getId() == a.getId() && a.getAuthorityIds().size() == 1
                        && a.getAuthorityIds().contains(authority.getId());
                }), "Expected authority '" + authorityName + "' not found in role '" + roleName2 + "'"));
        }

        @Test
        public void shouldSetUserRoles_withoutUserRole() {
            // GIVEN: New authority exists
            Authority authority = authorityService.save(fixtureMonkey.giveMeOne(Authority.class));
            String authorityName = authority.getAuthority();

            // GIVEN: Two new role exists (second one with authorities)
            Role role1 = roleService.save(fixtureMonkey.giveMeOne(Role.class));
            Role role2 = roleService.save(fixtureMonkey.giveMeBuilder(Role.class)
                .set(javaGetter(Role::getAuthorities), Set.of(authority)).sample());
            String roleName2 = role2.getName();

            // GIVEN: New user exists with new role
            User user = userService.save(fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getRoles), Set.of(role1, userRole)).sample());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role2.getId()), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Set user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
                new ParameterizedTypeReference<List<RoleDto>>() {});

            // THEN: Returns user roles, including 'USER' role
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertEquals(2, responseEntity.getBody().size()),
                () -> assertTrue(responseEntity.getBody().stream().anyMatch(a -> {
                    return role2.getId() == a.getId() && a.getAuthorityIds().size() == 1
                        && a.getAuthorityIds().contains(authority.getId());
                }), "Expected authority '" + authorityName + "' not found in role '" + roleName2 + "'"));
        }

        @Test
        public void shouldSetUserRoles_whenRolesNotFound() {
            // GIVEN: New authority exists
            Authority authority = authorityService.save(fixtureMonkey.giveMeOne(Authority.class));

            // GIVEN: New role exists with new authority
            Role role = roleService.save(fixtureMonkey.giveMeBuilder(Role.class)
                .set(javaGetter(Role::getAuthorities), Set.of(authority)).sample());

            // GIVEN: New user exists with new role
            User user = userService.save(fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getRoles), Set.of(role, userRole)).sample());

            // GIVEN: Admin authentication header with wrong role id
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role.getId() + 1), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Set user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
                new ParameterizedTypeReference<List<RoleDto>>() {});

            // THEN: Returns only the 'USER' role
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertEquals(1, responseEntity.getBody().size()));
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register).getBody();
            User user = userService.get(username);

            // GIVEN: User authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Set user roles
            ResponseEntity<ErrorDto> responseEntity =
                testRestTemplate.exchange(uri, HttpMethod.POST, request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Returns users
            assertAll(() -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage()));
        }

        @Test
        public void should404_whenNonexistentUser() {
            // GIVEN: New user exists
            User user = userService.save(
                fixtureMonkey.giveMeBuilder(User.class).set(javaGetter(User::getRoles), Set.of(userRole)).sample());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: Wrong user id in path
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(wrongId).toUri();

            // WHEN: Set user roles
            ResponseEntity<ErrorDto> responseEntity =
                testRestTemplate.exchange(uri, HttpMethod.POST, request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds not found
            assertAll(() -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User " + wrongId + " not found.", responseEntity.getBody().getMessage()));
        }

        @Test
        public void should409_whenRemovingOnlyAdminRole() {
            // GIVEN: Admin user exists
            User adminUser = userService.get("admin");

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: Admin user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(adminUser.getId()).toUri();

            // WHEN: Set user roles
            ResponseEntity<ErrorDto> responseEntity =
                testRestTemplate.exchange(uri, HttpMethod.POST, request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds conflict
            assertAll(() -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User must exist in the system with admin role.",
                    responseEntity.getBody().getMessage()));
        }
    }

    /**
     * {@link UserRoleController#addUserRoles} test.
     */
    @Nested
    public class AddUserRoles {
        private static final String ENDPOINT = "/users/{id}/roles";
        private final Role userRole = roleService.get(RoleService.USER_ROLE_NAME);

        @Test
        public void shouldAddUserRoles() {
            // GIVEN: New authority exists
            Authority authority = authorityService.save(fixtureMonkey.giveMeOne(Authority.class));
            String authorityName = authority.getAuthority();

            // GIVEN: Two new role exists (second one with authorities)
            Role role1 = roleService.save(fixtureMonkey.giveMeOne(Role.class));
            Role role2 = roleService.save(fixtureMonkey.giveMeBuilder(Role.class)
                .set(javaGetter(Role::getAuthorities), Set.of(authority)).sample());
            String roleName1 = role1.getName();
            String roleName2 = role2.getName();

            // GIVEN: New user exists with new role
            User user = userService.save(fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getRoles), Set.of(role1, userRole)).sample());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role2.getId()), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Add user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(uri, HttpMethod.PUT, request,
                new ParameterizedTypeReference<List<RoleDto>>() {});

            // THEN: Returns user roles
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertEquals(3, responseEntity.getBody().size()),
                () -> assertTrue(responseEntity.getBody().stream().anyMatch(a -> role1.getId() == a.getId()),
                    "Expected authority '" + authorityName + "' not found in role '" + roleName1 + "'"),
                () -> assertTrue(responseEntity.getBody().stream().anyMatch(a -> {
                    return role2.getId() == a.getId() && a.getAuthorityIds().size() == 1
                        && a.getAuthorityIds().contains(authority.getId());
                }), "Expected authority '" + authorityName + "' not found in role '" + roleName2 + "'"));
        }

        @Test
        public void shouldAddUserRoles_whenRolesNotFound() {
            // GIVEN: New authority exists
            Authority authority = authorityService.save(fixtureMonkey.giveMeOne(Authority.class));
            String authorityName = authority.getAuthority();

            // GIVEN: New role exists with new authority
            Role role = roleService.save(fixtureMonkey.giveMeBuilder(Role.class)
                .set(javaGetter(Role::getAuthorities), Set.of(authority)).sample());
            String roleName = role.getName();

            // GIVEN: New user exists with new role
            User user = userService.save(fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getRoles), Set.of(role, userRole)).sample());

            // GIVEN: Admin authentication header with wrong role id
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role.getId() + 1), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Add user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(uri, HttpMethod.PUT, request,
                new ParameterizedTypeReference<List<RoleDto>>() {});

            // THEN: Returns no user roles
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertEquals(2, responseEntity.getBody().size()),
                () -> assertTrue(responseEntity.getBody().stream().anyMatch(a -> {
                    return role.getId() == a.getId() && a.getAuthorityIds().size() == 1
                        && a.getAuthorityIds().contains(authority.getId());
                }), "Expected authority '" + authorityName + "' not found in role '" + roleName + "'"));
        }

        @Test
        public void shouldNotAddUserRoles_whenRolesAlreadyAdded() {
            // GIVEN: New authority exists
            Authority authority = authorityService.save(fixtureMonkey.giveMeOne(Authority.class));
            String authorityName = authority.getAuthority();

            // GIVEN: New role exists with new authority
            Role role = roleService.save(fixtureMonkey.giveMeBuilder(Role.class)
                .set(javaGetter(Role::getAuthorities), Set.of(authority)).sample());
            String roleName = role.getName();

            // GIVEN: New user exists with new role
            User user = userService.save(fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getRoles), Set.of(role, userRole)).sample());

            // GIVEN: Admin authentication header with wrong role id
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role.getId()), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Add user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(uri, HttpMethod.PUT, request,
                new ParameterizedTypeReference<List<RoleDto>>() {});

            // THEN: Returns no user roles
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertEquals(2, responseEntity.getBody().size()),
                () -> assertTrue(responseEntity.getBody().stream().anyMatch(a -> {
                    return role.getId() == a.getId() && a.getAuthorityIds().size() == 1
                        && a.getAuthorityIds().contains(authority.getId());
                }), "Expected authority '" + authorityName + "' not found in role '" + roleName + "'"));
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register).getBody();
            User user = userService.get(username);

            // GIVEN: User authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Add user roles
            ResponseEntity<ErrorDto> responseEntity =
                testRestTemplate.exchange(uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Returns users
            assertAll(() -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage()));
        }

        @Test
        public void should404_whenNonexistentUser() {
            // GIVEN: New user exists
            User user = userService.save(
                fixtureMonkey.giveMeBuilder(User.class).set(javaGetter(User::getRoles), Set.of(userRole)).sample());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: Wrong user id in path
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(wrongId).toUri();

            // WHEN: Add user roles
            ResponseEntity<ErrorDto> responseEntity =
                testRestTemplate.exchange(uri, HttpMethod.PUT, request, new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds not found
            assertAll(() -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User " + wrongId + " not found.", responseEntity.getBody().getMessage()));
        }
    }

    /**
     * {@link UserRoleController#removeUserRoles} test.
     */
    @Nested
    public class RemoveUserRoles {
        private static final String ENDPOINT = "/users/{id}/roles";
        private final Role userRole = roleService.get(RoleService.USER_ROLE_NAME);

        @Test
        public void shouldRemoveUserRoles() {
            // GIVEN: New authority exists
            Authority authority = authorityService.save(fixtureMonkey.giveMeOne(Authority.class));
            String authorityName = authority.getAuthority();

            // GIVEN: Two new role exists (second one with authorities)
            Role role1 = roleService.save(fixtureMonkey.giveMeOne(Role.class));
            Role role2 = roleService.save(fixtureMonkey.giveMeBuilder(Role.class)
                .set(javaGetter(Role::getAuthorities), Set.of(authority)).sample());
            String roleName2 = role2.getName();

            // GIVEN: New user exists with new role
            User user = userService.save(fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getRoles), Set.of(role1, role2, userRole)).sample());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role1.getId()), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Remove user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(uri, HttpMethod.DELETE, request,
                new ParameterizedTypeReference<List<RoleDto>>() {});

            // THEN: Returns user roles without deleted role
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertEquals(2, responseEntity.getBody().size()),
                () -> assertTrue(responseEntity.getBody().stream().anyMatch(a -> {
                    return role2.getId() == a.getId() && a.getAuthorityIds().size() == 1
                        && a.getAuthorityIds().contains(authority.getId());
                }), "Expected authority '" + authorityName + "' not found in role '" + roleName2 + "'"));
        }

        @Test
        public void shouldNotRemoveUserRoles_whenRolesNotFound() {
            // GIVEN: New authority exists
            Authority authority = authorityService.save(fixtureMonkey.giveMeOne(Authority.class));
            String authorityName = authority.getAuthority();

            // GIVEN: New role exists with new authority
            Role role = roleService.save(fixtureMonkey.giveMeBuilder(Role.class)
                .set(javaGetter(Role::getAuthorities), Set.of(authority)).sample());
            String roleName = role.getName();

            // GIVEN: New user exists with new role
            User user = userService.save(fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getRoles), Set.of(role, userRole)).sample());

            // GIVEN: Admin authentication header with wrong role id
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role.getId() + 1), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Remove user roles
            ResponseEntity<List<RoleDto>> responseEntity = testRestTemplate.exchange(uri, HttpMethod.DELETE, request,
                new ParameterizedTypeReference<List<RoleDto>>() {});

            // THEN: Returns no user roles
            assertAll(() -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()), () -> assertEquals(2, responseEntity.getBody().size()),
                () -> assertTrue(responseEntity.getBody().stream().anyMatch(a -> {
                    return role.getId() == a.getId() && a.getAuthorityIds().size() == 1
                        && a.getAuthorityIds().contains(authority.getId());
                }), "Expected authority '" + authorityName + "' not found in role '" + roleName + "'"));
        }

        @Test
        public void should400_whenRemoveUserRole() {
            // GIVEN: New authority exists
            Authority authority = authorityService.save(fixtureMonkey.giveMeOne(Authority.class));

            // GIVEN: Two new role exists (second one with authorities)
            Role role1 = roleService.save(fixtureMonkey.giveMeOne(Role.class));
            Role role2 = roleService.save(fixtureMonkey.giveMeBuilder(Role.class)
                .set(javaGetter(Role::getAuthorities), Set.of(authority)).sample());

            // GIVEN: New user exists with new role
            User user = userService.save(fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getRoles), Set.of(role1, role2, userRole)).sample());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(role1.getId(), userRole.getId()), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Remove user roles
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(uri, HttpMethod.DELETE, request,
                new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Returns user roles without deleted role
            assertAll(() -> assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Role '" + RoleService.USER_ROLE_NAME + "' cannot be removed from users.",
                    responseEntity.getBody().getMessage()));
        }

        @Test
        public void should403_whenUnauthorized() {
            // GIVEN: New user registered
            RegisterDto register = fixtureMonkey.giveMeOne(RegisterDto.class);
            String username = register.getUsername();
            AuthenticationDto auth = authenticationController.register(register).getBody();
            User user = userService.get(username);

            // GIVEN: User authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(auth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: New user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(user.getId()).toUri();

            // WHEN: Remove user roles
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(uri, HttpMethod.DELETE, request,
                new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Returns users
            assertAll(() -> assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("Access Denied", responseEntity.getBody().getMessage()));
        }

        @Test
        public void should404_whenNonexistentUser() {
            // GIVEN: New user exists
            User user = userService.save(
                fixtureMonkey.giveMeBuilder(User.class).set(javaGetter(User::getRoles), Set.of(userRole)).sample());

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(Collections.emptyList(), headers);

            // GIVEN: Wrong user id in path
            int wrongId = user.getId() + 1;
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(wrongId).toUri();

            // WHEN: Remove user roles
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(uri, HttpMethod.DELETE, request,
                new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds not found
            assertAll(() -> assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User " + wrongId + " not found.", responseEntity.getBody().getMessage()));
        }

        @Test
        public void should409_whenRemovingOnlyAdminRole() {
            // GIVEN: Admin user exists
            User adminUser = userService.get("admin");
            Role adminRole = roleService.get(RoleService.ADMIN_ROLE_NAME);

            // GIVEN: Admin authentication header
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminAuth.getAccessToken());
            HttpEntity<List<Integer>> request = new HttpEntity<>(List.of(adminRole.getId()), headers);

            // GIVEN: Admin user id in path
            URI uri = UriComponentsBuilder.fromUriString(url).path(ENDPOINT).buildAndExpand(adminUser.getId()).toUri();

            // WHEN: Remove user roles
            ResponseEntity<ErrorDto> responseEntity = testRestTemplate.exchange(uri, HttpMethod.DELETE, request,
                new ParameterizedTypeReference<ErrorDto>() {});

            // THEN: Responds conflict
            assertAll(() -> assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(clock.instant(), responseEntity.getBody().getTimestamp()),
                () -> assertEquals("User must exist in the system with admin role.",
                    responseEntity.getBody().getMessage()));
        }
    }
}
