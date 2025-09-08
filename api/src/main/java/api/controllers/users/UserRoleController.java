package api.controllers.users;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.ErrorDto;
import api.dtos.RoleDto;
import api.entities.Role;
import api.entities.User;
import api.mapper.RoleMapper;
import api.services.RoleService;
import api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * {@link UserRoleController}.
 */
@RestController
@RequestMapping("/users/{userId}/roles")
@Tag(name = "User Roles", description = "The roles applied to a specific user.")
public class UserRoleController {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleMapper roleMapper;

    /**
     * Get user's roles.
     *
     * @param userId User id
     * @return {@link List} of {@link RoleDto}
     */
    // region
    @Operation(
        summary = "Get User's Roles",
        description = "Get list of roles applied to a user."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = RoleDto.class)),
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "403",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @GetMapping("")
    @PreAuthorize("hasAuthority(@DbSetup.USER_READ) and hasAuthority(@DbSetup.ROLE_READ)")
    public List<RoleDto> getUserRoles(@PathVariable int userId) {
        Set<Role> roles = userService.getRoles(userId);
        return roles.stream()
            .map(roleMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Set user's roles.
     *
     * @param userId User id
     * @param roleIds Role ids
     * @return {@link List} of {@link RoleDto}
     * @apiNote Does not throw error if roles not found
     */
    // region
    @Operation(
        summary = "Set User's Roles",
        description = "Set list of roles applied to a user."
            + "<ul>"
                + "<li>If a role ID is not found in the system, it will not be applied to the user.</li>"
            + "</ul>"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = RoleDto.class)),
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "403",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @PostMapping("")
    @PreAuthorize("hasAuthority(@DbSetup.USER_WRITE) and hasAuthority(@DbSetup.ROLE_READ)")
    public List<RoleDto> setUserRoles(@PathVariable int userId, @RequestBody List<Integer> roleIds) {
        User user = userService.get(userId);
        user.setRoles(new HashSet<>(roleService.get(roleIds)));
        userService.save(user);

        return userService.getRoles(userId).stream()
            .map(roleMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Add roles to user.
     *
     * @param userId User id
     * @param roleIds Role ids
     * @return {@link List} of {@link RoleDto}
     * @apiNote Does not throw error if user already has specified role
     * @apiNote Does not throw error if roles not found
     */
    // region
    @Operation(
        summary = "Add User's Roles",
        description = "Add list of roles applied to a user."
            + "<ul>"
                + "<li>If a role ID is not found in the system, it will not be applied to the user.</li>"
                + "<li>If a role ID is already applied to the user, it will remain applied.</li>"
            + "</ul>"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = RoleDto.class)),
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "403",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @PutMapping("")
    @PreAuthorize("""
        hasAuthority(@DbSetup.USER_READ)
        and hasAuthority(@DbSetup.USER_WRITE)
        and hasAuthority(@DbSetup.ROLE_READ)
        """)
    public List<RoleDto> addUserRoles(@PathVariable int userId, @RequestBody List<Integer> roleIds) {
        Set<Role> roles = userService.getRoles(userId);
        roles.addAll(roleService.get(roleIds));

        User user = userService.get(userId);
        user.setRoles(roles);
        userService.save(user);

        return userService.getRoles(userId).stream()
            .map(roleMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Delete roles from user.
     *
     * @param userId User id
     * @param roleIds Role ids
     * @return {@link List} of {@link RoleDto}
     * @apiNote Does not throw error if user does not have specified role
     */
    // region
    @Operation(
        summary = "Remove User's Roles",
        description = "Remove list of roles applied to a user."
            + "<ul>"
                + "<li>If a role ID is not applied to the user, it will remain not applied.</li>"
            + "</ul>"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = RoleDto.class)),
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "403",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @DeleteMapping("")
    @PreAuthorize("""
        hasAuthority(@DbSetup.USER_READ)
        and hasAuthority(@DbSetup.USER_WRITE)
        and hasAuthority(@DbSetup.ROLE_READ)
        """)
    public List<RoleDto> removeUserRoles(@PathVariable int userId, @RequestBody List<Integer> roleIds) {
        Set<Role> roles = userService.getRoles(userId);
        roles.removeAll(roleService.get(roleIds));

        User user = userService.get(userId);
        user.setRoles(roles);
        userService.save(user);

        return userService.getRoles(userId).stream()
            .map(roleMapper::toDto)
            .collect(Collectors.toList());
    }
}
