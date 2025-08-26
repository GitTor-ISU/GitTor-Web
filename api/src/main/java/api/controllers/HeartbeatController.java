package api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * {@link HeartbeatController}.
 */
@RestController
@RequestMapping("")
@Tag(name = "Heartbeat", description = "Verify the backend is running.")
public class HeartbeatController {
    /**
     * Heartbeat.
     *
     * @return true
     */
    @GetMapping("")
    @Operation(
        summary = "Heartbeat",
        description = "Always returns <em>true</em>."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                schema = @Schema(implementation = Boolean.class),
                mediaType = "application/json"
            )
        ),
    })
    public boolean heartbeat() {
        return true;
    }
}
