package api.controllers.users;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
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

    @Value("${api.avatar.max:5000000}")
    private long maxSize;

    private static final Set<MediaType> ALLOWED_TYPES = Set.of(
        MediaType.IMAGE_PNG,
        MediaType.IMAGE_JPEG,
        MediaType.parseMediaType("image/svg+xml"),
        MediaType.IMAGE_GIF
    );

    /**
     * Get my avatar.
     *
     * @param user User
     * @return {@link ResponseEntity} {@link Resource}
     * @throws IOException Failed to read file
     */
    @Transactional(readOnly = true)
    @GetMapping("/me/avatar")
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
    public ResponseEntity<Resource> getMyAvatar(@AuthenticationPrincipal User user) throws IOException {
        S3Object s3Object = user.getAvatar();
        if (s3Object == null) {
            throw new EntityNotFoundException("User '" + user.getUsername() +  "' avatar not found.");
        }

        InputStream in = s3ObjectService.download(s3Object);

        byte[] bytes = in.readAllBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(s3Object.getMediaType()))
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
    @Transactional
    @PutMapping("/me/avatar")
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
    public void updateMyAvatar(
        @AuthenticationPrincipal User user,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty.");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds limit (5MB).");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(MediaType.parseMediaType(contentType))) {
            throw new IllegalArgumentException("Only PNG, JPEG, SVG and GIF images are allowed.");
        }

        final S3Object oldAvatar = user.getAvatar();
        S3Object newAvatar = S3Object.builder()
            .key(S3ObjectService.AVATAR_DIR + user.getId() + "_" + UUID.randomUUID())
            .size(file.getSize())
            .mediaType(contentType)
            .build();

        s3ObjectService.save(newAvatar, file.getInputStream());
        user.setAvatar(newAvatar);
        userService.save(user);

        if (oldAvatar != null) {
            s3ObjectService.delete(oldAvatar);
        }
    }

    /**
     * Delete my avatar.
     *
     * @param user User
     */
    @Transactional
    @DeleteMapping("/me/avatar")
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
    public void deleteMyAvatar(@AuthenticationPrincipal User user) {
        S3Object s3Object = user.getAvatar();
        if (s3Object == null) {
            throw new EntityNotFoundException("User '" + user.getUsername() +  "' avatar not found.");
        }

        user.setAvatar(null);
        userService.save(user);
        s3ObjectService.delete(s3Object);
    }


    /**
     * Get user's avatar.
     *
     * @param userId User id
     * @return User's avatar
     * @throws IOException Failed to read file
     */
    @Transactional(readOnly = true)
    @GetMapping("/{userId}/avatar")
    @PreAuthorize("hasAuthority('USER_READ')")
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
    public ResponseEntity<Resource> getUserAvatar(@PathVariable int userId) throws IOException {
        User user = userService.get(userId);
        S3Object s3Object = user.getAvatar();
        if (s3Object == null) {
            throw new EntityNotFoundException("User '" + user.getUsername() +  "' avatar not found.");
        }

        InputStream in = s3ObjectService.download(s3Object);

        byte[] bytes = in.readAllBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(s3Object.getMediaType()))
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
    @Transactional
    @PutMapping("/{userId}/avatar")
    @PreAuthorize("hasAuthority('USER_WRITE')")
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
    public void updateUserAvatar(
        @PathVariable int userId,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty.");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds limit (5MB).");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(MediaType.parseMediaType(contentType))) {
            throw new IllegalArgumentException("Only PNG, JPEG, SVG and GIF images are allowed.");
        }

        User user = userService.get(userId);
        final S3Object oldAvatar = user.getAvatar();
        S3Object newAvatar = S3Object.builder()
            .key(S3ObjectService.AVATAR_DIR + user.getId() + "_" + UUID.randomUUID())
            .size(file.getSize())
            .mediaType(contentType)
            .build();

        s3ObjectService.save(newAvatar, file.getInputStream());
        user.setAvatar(newAvatar);
        userService.save(user);

        if (oldAvatar != null) {
            s3ObjectService.delete(oldAvatar);
        }
    }

    /**
     * Delete user's avatar.
     *
     * @param userId User id
     */
    @Transactional
    @DeleteMapping("/{userId}/avatar")
    @PreAuthorize("hasAuthority('USER_WRITE')")
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
    public void deleteUserAvatar(@PathVariable int userId) {
        User user = userService.get(userId);
        S3Object s3Object = user.getAvatar();
        if (s3Object == null) {
            throw new EntityNotFoundException("User '" + user.getUsername() +  "' avatar not found.");
        }

        user.setAvatar(null);
        userService.save(user);
        s3ObjectService.delete(s3Object);
    }
}
