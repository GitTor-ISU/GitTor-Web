package api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link CreateTorrentDto}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTorrentDto {
    private String name;
    private String description;
}
