package api.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.AuthorityDto;
import api.dtos.ErrorDto;
import api.mapper.AuthorityMapper;
import api.services.AuthorityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * {@link AuthorityController}.
 */
@RestController
@RequestMapping("/authorities")
@Tag(
    name = "Authorities",
    description = "Authorites can be applied to roles and specify what functionality that role has access to."
)
public class AuthorityController {
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private AuthorityMapper authorityMapper;

    /**
     * Get authorities.
     *
     * @return {@link List} of {@link AuthorityDto}
     */
    // region
    @Operation(
        summary = "Get Authorities",
        description = "Get list of all authorities."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = AuthorityDto.class)),
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
    @PreAuthorize("hasAuthority(@DbSetup.AUTHORITY_READ)")
    public List<AuthorityDto> getAuthorities() {
        return authorityService.getAll().stream()
            .map(authorityMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get authority.
     *
     * @param id Authority id
     * @return {@link AuthorityDto}
     */
    // region
    @Operation(
        summary = "Get Authority",
        description = "Get specific authority."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                schema = @Schema(implementation = AuthorityDto.class),
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
    @PreAuthorize("hasAuthority(@DbSetup.AUTHORITY_READ)")
    public AuthorityDto getAuthority(@PathVariable int id) {
        return authorityMapper.toDto(authorityService.get(id));
    }
}
