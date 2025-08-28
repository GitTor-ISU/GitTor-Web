package api.controllers;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.AuthenticationDto;
import api.dtos.ErrorDto;
import api.dtos.LoginDto;
import api.dtos.RegisterDto;
import api.entities.Role;
import api.entities.User;
import api.exceptions.DuplicateEntityException;
import api.services.RoleService;
import api.services.TokenService;
import api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * {@link AuthenticationController}.
 */
@RestController
@RequestMapping("/authenticate")
@Tag(name = "Authentication", description = "Handles user login and registration.")
public class AuthenticationController {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Login.
     *
     * @param login {@link LoginDto}
     * @return {@link AuthenticationDto}
     */
    // region
    @Operation(
        summary = "Login",
        description = "Login to an existing user."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                schema = @Schema(implementation = AuthenticationDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @Transactional(readOnly = true)
    @PostMapping("/login")
    public AuthenticationDto login(@RequestBody LoginDto login) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));

        return tokenService.generateToken(authentication);
    }

    /**
     * Register.
     *
     * @param register {@link RegisterDto}
     * @return {@link AuthenticationDto}
     */
    // region
    @Operation(
        summary = "Register",
        description = "Register a new user."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                schema = @Schema(implementation = AuthenticationDto.class),
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
    @Transactional
    @PostMapping("/register")
    public AuthenticationDto register(@RequestBody RegisterDto register) {
        if (!StringUtils.hasText(register.getUsername())) {
            throw new IllegalArgumentException("Username must not be empty.");
        }
        if (!StringUtils.hasText(register.getPassword())) {
            throw new IllegalArgumentException("Password must not be empty.");
        }
        if (userService.exists(register.getUsername())) {
            throw new DuplicateEntityException("Username '" + register.getUsername() + "' already exists.");
        }

        Role userRole = roleService.get("USER");

        User user = User.builder()
            .username(register.getUsername())
            .password(encoder.encode(register.getPassword()))
            .roles(Set.of(userRole))
            .build();

        userService.save(user);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(register.getUsername(), register.getPassword()));

        return tokenService.generateToken(authentication);
    }
}
