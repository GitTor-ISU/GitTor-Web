package api.dtos;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link UserDto}.
 * <p>
 * Validations defined only apply on incoming request bodies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Null
    private Integer id;

    @Nullable
    @Size(min = 3, max = 255)
    @Email
    private String email;

    @Nullable
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    private String username;

    @Nullable
    @Size(min = 1, max = 35)
    private String firstname;

    @Nullable
    @Size(min = 1, max = 35)
    private String lastname;
}
