package api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link UpdateTorrentDto}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTorrentDto {
    private String name;
    private String description;
}
