package api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link RegisterDto}.
 *
 * <p>Validations defined only apply on incoming request bodies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {
    @NotNull
    @Size(min = 3, max = 255)
    @Email
    private String email;

    @NotNull
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    private String username;

    @NotNull
    @Size(min = 8, max = 72)
    private String password;
}
