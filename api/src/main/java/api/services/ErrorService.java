package api.services;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.dtos.ErrorDto;

/**
 * {@link ErrorService}.
 */
@Service
public class ErrorService {
    @Autowired
    private Clock clock;

    /**
     * Create {@link ErrorDto}.
     *
     * @param message Error message
     * @return {@link ErrorDto}
     */
    public ErrorDto error(String message) {
        return new ErrorDto(message, Instant.now(clock));
    }
}
