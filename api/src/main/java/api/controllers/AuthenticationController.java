package api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.AuthenticationDto;
import api.dtos.ErrorDto;
import api.dtos.LoginDto;
import api.dtos.RegisterDto;
import api.entities.RefreshToken;
import api.entities.User;
import api.services.AuthenticationService;
import api.services.TokenService;
import api.services.UserService;
import api.utils.CookieUtils;
import ch.qos.logback.core.util.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * {@link AuthenticationController}.
 */
@RestController
@RequestMapping("/authenticate")
@Tag(name = "Authentication", description = "Handles user login and registration.")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Login.
     *
     * @param login {@link LoginDto}
     * @return {@link ResponseEntity} {@link AuthenticationDto}
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
    @PostMapping("/login")
    public ResponseEntity<AuthenticationDto> login(@Valid @RequestBody LoginDto login) {
        User user = userService.find(
            StringUtil.isNullOrEmpty(login.getUsername())
            ? login.getEmail()
            : login.getUsername()
        ).orElse(null);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user, login.getPassword()));

        RefreshToken refreshToken = tokenService.getOrGenerateRefreshToken(user);
        AuthenticationDto authDto = tokenService.generateAccessToken(authentication.getName());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, CookieUtils.generateRefreshTokenCookie(refreshToken).toString())
            .body(authDto);
    }

    /**
     * Register.
     *
     * @param registerDto {@link RegisterDto}
     * @return {@link ResponseEntity} {@link AuthenticationDto}
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
    @PostMapping("/register")
    public ResponseEntity<AuthenticationDto> register(@Valid @RequestBody RegisterDto registerDto) {
        User user = authenticationService.register(registerDto);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user, registerDto.getPassword()));

        RefreshToken refreshToken = tokenService.getOrGenerateRefreshToken(user);
        AuthenticationDto authDto = tokenService.generateAccessToken(authentication.getName());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, CookieUtils.generateRefreshTokenCookie(refreshToken).toString())
            .body(authDto);
    }

    /**
     * Refresh a user.
     *
     * @param refreshToken {@link String}
     * @return {@link AuthenticationDto}
     */
    // region
    @Operation(
        summary = "Refresh",
        description = "Refresh a user."
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
            responseCode = "401",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        )
    })
    // endregion
    @GetMapping("/refresh")
    public AuthenticationDto refresh(@CookieValue(CookieUtils.REFRESH_COOKIE_NAME) String refreshToken) {
        String username = tokenService.validate(refreshToken);
        return tokenService.generateAccessToken(username);
    }

    /**
     * Logout a user.
     *
     * @param refreshToken {@link String}
     * @return {@link ResponseEntity} {@link Void}
     */
    // region
    @Operation(
        summary = "Logout",
        description = "Logout a user."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204"
        ),
        @ApiResponse(
            responseCode = "401",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(CookieUtils.REFRESH_COOKIE_NAME) String refreshToken) {
        tokenService.invalidate(refreshToken);
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, CookieUtils.generateEmptyCookie().toString())
            .build();
    }
}
