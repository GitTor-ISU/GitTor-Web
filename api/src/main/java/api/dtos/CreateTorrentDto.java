package api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank
    @Size(min = 3, max = 255)
    private String name;
    private String description;
}
