package api.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * {@link HeartbeatController} test..
 */
public class HeartbeatControllerTest extends BasicControllerTest {
    /**
     * {@link HeartbeatController#heartbeat} test.
     */
    @Test
    public void shouldReturnTrue_whenHeartbeat() {
        // GIVEN:
        // WHEN: Call endpoint
        ResponseEntity<Boolean> responseEntity = testRestTemplate.exchange(
            url + "/", HttpMethod.GET, null, new ParameterizedTypeReference<Boolean>() {}
        );

        // THEN: Returns OK and true
        assertAll(
            () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
            () -> assertEquals(true, responseEntity.getBody())
        );
    }
}
