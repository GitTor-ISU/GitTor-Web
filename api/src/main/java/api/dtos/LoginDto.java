package api.dtos;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link LoginDto}.
 *
 * <p>
 * Validations defined only apply on incoming request bodies.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
    @Nullable
    @Size(min = 3, max = 255)
    @Email
    private String email;

    @Nullable
    private String username;

    @NotNull
    private String password;
}
