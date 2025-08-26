package api.dtos;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link ErrorDto}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {
    private String message;
    private Instant timestamp;
}
