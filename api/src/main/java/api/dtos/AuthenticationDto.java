package api.dtos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link AuthenticationDto}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationDto {
    private String accessToken;
    private String tokenType;
    private Date expires;
}
