package api.exceptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import api.dtos.ErrorDto;
import api.services.ErrorService;
import api.utils.CookieUtils;

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

    /**
     * Handle {@link IllegalStateException}.
     *
     * @param ex Exception
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleIllegalStateException(IllegalStateException ex) {
        return new ResponseEntity<>(errorService.error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    /**
     * Handle {@link MethodArgumentNotValidException}.
     *
     * @param ex Exception
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String field = StringUtils.capitalize(ex.getFieldError().getField());
        String errorMessage = String.format("%s %s.", field, ex.getFieldError().getDefaultMessage());
        return new ResponseEntity<>(errorService.error(errorMessage), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle {@link MissingRequestCookieException}.
     *
     * @param ex Exception
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorDto> handleMissingRequestCookieException(MissingRequestCookieException ex) {
        return new ResponseEntity<>(errorService.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle {@link RefreshTokenException}.
     *
     * @param ex Exception
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorDto> handleRefreshTokenException(RefreshTokenException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, CookieUtils.generateEmptyCookie().toString());
        return new ResponseEntity<>(errorService.error(ex.getMessage()), headers, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle {@link BadCredentialsException}.
     *
     * @param ex Exception
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDto> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>(errorService.error(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }
}
