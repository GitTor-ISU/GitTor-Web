package api.exceptions;

/**
 * {@link EntityNotFoundException}.
 */
public class EntityNotFoundException extends RuntimeException {
    /**
     * EntityNotFoundException.
     *
     * @param message Error message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
