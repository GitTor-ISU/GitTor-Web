package api.controllers.users;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import api.dtos.ErrorDto;
import api.entities.S3Object;
import api.entities.User;
import api.exceptions.EntityNotFoundException;
import api.services.S3ObjectService;
import api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * {@link UserAvatarController}.
 */
@RestController
@RequestMapping("/users")
@Tag(name = "User Avatars", description = "The avatars displayed for a user.")
public class UserAvatarController {
    @Autowired
    private S3ObjectService s3ObjectService;
    @Autowired
    private UserService userService;

    /**
     * Get my avatar.
     *
     * @param user User
     * @return {@link ResponseEntity} {@link Resource}
     * @throws IOException Failed to read file
     */
    // region
    @Operation(
        summary = "Get My Avatar",
        description = "Gets the current user's avatar."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = {
                @Content(mediaType = "image/png"),
                @Content(mediaType = "image/jpeg"),
                @Content(mediaType = "image/gif"),
                @Content(mediaType = "image/svg+xml")
            }
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
    @GetMapping("/me/avatar")
    public ResponseEntity<Resource> getMyAvatar(@AuthenticationPrincipal User user) throws IOException {
        S3Object s3Object = user.getAvatar();
        if (s3Object == null) {
            throw EntityNotFoundException.fromUserAvatar(user.getUsername());
        }

        InputStream in = s3ObjectService.download(s3Object);

        byte[] bytes = in.readAllBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(s3Object.getMimeType().getName()))
            .contentLength(bytes.length)
            .body(resource);
    }

    /**
     * Update my avatar.
     *
     * @param user User
     * @param file Avatar
     * @throws IOException Failed to read file
     */
    // region
    @Operation(
        summary = "Update My Avatar",
        description = "Updates the current user's avatar."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                schema = @Schema(implementation = Void.class),
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
    })
    // endregion
    @PutMapping("/me/avatar")
    public void updateMyAvatar(
        @AuthenticationPrincipal User user,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        userService.updateAvatar(user, file);
    }

    /**
     * Delete my avatar.
     *
     * @param user User
     */
    // region
    @Operation(
        summary = "Delete My Avatar",
        description = "Deletes the current user's avatar."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                schema = @Schema(implementation = Void.class),
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
    @DeleteMapping("/me/avatar")
    public void deleteMyAvatar(@AuthenticationPrincipal User user) {
        userService.deleteAvatar(user);
    }


    /**
     * Get user's avatar.
     *
     * @param userId User id
     * @return User's avatar
     * @throws IOException Failed to read file
     */
    // region
    @Operation(
        summary = "Get User's Avatar",
        description = "Gets specified user's avatar."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = {
                @Content(mediaType = "image/png"),
                @Content(mediaType = "image/jpeg"),
                @Content(mediaType = "image/gif"),
                @Content(mediaType = "image/svg+xml")
            }
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
    @GetMapping("/{userId}/avatar")
    @PreAuthorize("hasAuthority(@DbSetup.USER_READ)")
    public ResponseEntity<Resource> getUserAvatar(@PathVariable int userId) throws IOException {
        User user = userService.get(userId);
        S3Object s3Object = user.getAvatar();
        if (s3Object == null) {
            throw EntityNotFoundException.fromUserAvatar(user.getUsername());
        }

        InputStream in = s3ObjectService.download(s3Object);

        byte[] bytes = in.readAllBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(s3Object.getMimeType().getName()))
            .contentLength(bytes.length)
            .body(resource);
    }

    /**
     * Update user's avatar.
     *
     * @param userId User id
     * @param file User's avatar
     * @throws IOException Failed to read file
     */
    // region
    @Operation(
        summary = "Update User's Avatar",
        description = "Updates specified user's avatar."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                schema = @Schema(implementation = Void.class),
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
            responseCode = "404",
            content = @Content(
                schema = @Schema(implementation = ErrorDto.class),
                mediaType = "application/json"
            )
        ),
    })
    // endregion
    @PutMapping("/{userId}/avatar")
    @PreAuthorize("hasAuthority(@DbSetup.USER_WRITE)")
    public void updateUserAvatar(
        @PathVariable int userId,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        userService.updateAvatar(userId, file);
    }

    /**
     * Delete user's avatar.
     *
     * @param userId User id
     */
    // region
    @Operation(
        summary = "Delete User's Avatar",
        description = "Deletes specified user's avatar."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = {
                @Content(mediaType = "image/png"),
                @Content(mediaType = "image/jpeg"),
                @Content(mediaType = "image/gif"),
                @Content(mediaType = "image/svg+xml")
            }
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
    @DeleteMapping("/{userId}/avatar")
    @PreAuthorize("hasAuthority(@DbSetup.USER_WRITE)")
    public void deleteUserAvatar(@PathVariable int userId) {
        userService.deleteAvatar(userId);
    }
}
