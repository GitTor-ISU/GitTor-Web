package api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.AuthenticationDto;
import api.dtos.ErrorDto;
import api.dtos.LoginDto;
import api.dtos.RegisterDto;
import api.entities.User;
import api.services.AuthenticationService;
import api.services.TokenService;
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
    private AuthenticationService authenticationService;
    @Autowired
    private TokenService tokenService;
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
    @PostMapping("/login")
    public AuthenticationDto login(@RequestBody LoginDto login) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));

        return tokenService.generateToken(authentication);
    }

    /**
     * Register.
     *
     * @param registerDto {@link RegisterDto}
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
    @PostMapping("/register")
    public AuthenticationDto register(@RequestBody RegisterDto registerDto) {
        User user = authenticationService.register(registerDto);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getUsername(), registerDto.getPassword()));

        return tokenService.generateToken(authentication);
    }
}
