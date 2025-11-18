package api.dtos;

import java.time.LocalDateTime;
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
    private Long id;
    private String name;
    private String description;
    private Long fileSize;
    private Integer uploaderId;
    private String uploaderUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
