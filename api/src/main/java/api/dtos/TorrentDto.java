package api.dtos;

import java.time.LocalDateTime;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link TorrentDto}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TorrentDto {
    @Null
    private Long id;

    @Nullable
    @Size(min = 3, max = 255)
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    private String name;

    @Nullable
    @Size(min = 3, max = 255)
    private String description;

    private Long fileSize;
    private Integer uploaderId;
    private String uploaderUsername;

    @Null
    private LocalDateTime createdAt;
    @Null
    private LocalDateTime updatedAt;
}
