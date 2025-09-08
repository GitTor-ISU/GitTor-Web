package api.controllers.users;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.ErrorDto;
import api.dtos.UserDto;
import api.entities.User;
import api.mapper.UserMapper;
import api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * {@link UserController}.
 */
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "System users.")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder encoder;

    @Value("${pagination.default-page-size:10}")
    private int defaultPageSize;
    @Value("${pagination.max-page-size:100}")
    private int maxPageSize;

    /**
     * Get me.
     *
     * @param user User
     * @return {@link UserDto}
     */
    // region
    @Operation(
        summary = "Get Me",
        description = "Get the current user's information."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                schema = @Schema(implementation = UserDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @GetMapping("/me")
    public UserDto getMe(@AuthenticationPrincipal User user) {
        return userMapper.toDto(user);
    }

    /**
     * Update me.
     *
     * @param user User
     * @param userDto Updated info
     * @return {@link UserDto}
     */
    // region
    @Operation(
        summary = "Update Me",
        description = "Update the current user's information."
            + "<ul>"
                + "<li>Ignores <em>id</em> and <em>password</em> from request body.</li>"
                + "<li>Any field missing or null from request body will be left unchanged in user information.</li>"
            + "</ul>"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                schema = @Schema(implementation = UserDto.class),
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "400",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
        @ApiResponse(
            responseCode = "409",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @PutMapping("/me")
    public UserDto updateMe(@AuthenticationPrincipal User user, @RequestBody UserDto userDto) {
        return userMapper.toDto(userService.update(user, userDto));
    }

    /**
     * Update my password.
     *
     * @param user User
     * @param password New password
     */
    // region
    @Operation(
        summary = "Update My Password",
        description = "Updates the current user's password."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200"
        ),
        @ApiResponse(
            responseCode = "400",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @PutMapping("/me/password")
    public void updateMyPassword(@AuthenticationPrincipal User user, @RequestBody String password) {
        if (encoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("New password matches existing password.");
        }

        user.setPassword(encoder.encode(password));
        userService.save(user);
    }

    /**
     * Delete me.
     *
     * @param user User
     */
    // region
    @Operation(
        summary = "Delete Me",
        description = "Deletes the current user."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200"
        ),
    })
    // endregion
    @DeleteMapping("/me")
    public void deleteMe(@AuthenticationPrincipal User user) {
        userService.delete(user);
    }

    /**
     * Get users.
     *
     * @param page Page index
     * @param size Page size
     * @return {@link List} of {@link UserDto}
     */
    // region
    @Operation(
        summary = "Get Users",
        description = "Get paginated list of user's information."
            + "<ul>"
                + "<li>If <em>size</em> excedes limit, response will contain up to limit.</li>"
            + "</ul>"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = UserDto.class)),
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
    })
    // endregion
    @GetMapping("")
    @PreAuthorize("hasAuthority(@DbSetup.USER_READ)")
    public List<UserDto> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) Integer size
    ) {
        int requestedSize = (size != null) ? size : defaultPageSize;
        int safeSize = Math.min(requestedSize, maxPageSize);

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("id").ascending());
        return userService.getAll(pageable)
            .map(userMapper::toDto)
            .getContent();
    }

    /**
     * Get user.
     *
     * @param id User id
     * @return {@link UserDto}
     */
    // region
    @Operation(
        summary = "Get User",
        description = "Get specific user's information."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                schema = @Schema(implementation = UserDto.class),
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
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@DbSetup.USER_READ)")
    public UserDto getUser(@PathVariable int id) {
        User user = userService.get(id);
        Hibernate.initialize(user);
        return userMapper.toDto(user);
    }

    /**
     * Update user.
     *
     * @param id User id
     * @param userDto Updated info
     * @return {@link UserDto}
     */
    // region
    @Operation(
        summary = "Update User",
        description = "Update specific user's information.<br>"
            + "<ul>"
                + "<li>Ignores <em>id</em> and <em>password</em> from request body.</li>"
                + "<li>Any field missing or null from request body will be left unchanged in user information.</li>"
            + "</ul>"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = Integer.class)),
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
        @ApiResponse(
            responseCode = "409",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(@DbSetup.USER_READ) and hasAuthority(@DbSetup.USER_WRITE)")
    public UserDto updateUser(@PathVariable int id, @RequestBody UserDto userDto) {
        return userMapper.toDto(userService.update(id, userDto));
    }

    /**
     * Delete user.
     *
     * @param id User id
     */
    // region
    @Operation(
        summary = "Delete User",
        description = "Deletes specific user."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200"
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
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(@DbSetup.USER_WRITE)")
    public void deleteUser(@PathVariable int id) {
        userService.delete(id);
    }
}
