package api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link AuthorityDto}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityDto {
    private Integer id;
    private String authority;
}
