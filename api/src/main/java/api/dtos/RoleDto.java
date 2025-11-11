package api.dtos;

import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link RoleDto}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    @Nullable
    private Integer id;

    @Nullable
    @Size(min = 1, max = 255)
    private String name;

    @Nullable
    private List<Integer> authorityIds;
}
