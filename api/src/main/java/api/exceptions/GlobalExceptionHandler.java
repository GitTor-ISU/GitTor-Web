package api.exceptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import api.dtos.ErrorDto;
import api.services.ErrorService;

/**
 * {@link GlobalExceptionHandler}.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private ErrorService errorService;

    /**
     * Handle {@link EntityNotFoundException}.
     *
     * @param ex Exception
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>(errorService.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * Handle {@link DuplicateEntityException}.
     *
     * @param ex Exception
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ErrorDto> handleDuplicateEntityException(DuplicateEntityException ex) {
        return new ResponseEntity<>(errorService.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    /**
     * Handle {@link IllegalArgumentException}.
     *
     * @param ex Exception
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(errorService.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle {@link AccessDeniedException}.
     *
     * @param ex Exception
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(errorService.error(ex.getMessage()), HttpStatus.FORBIDDEN);
    }
}
