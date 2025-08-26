package api.exceptions;

/**
 * {@link DuplicateEntityException}.
 */
public class DuplicateEntityException extends RuntimeException {
    /**
     * DuplicateEntityException.
     *
     * @param message Error message
     */
    public DuplicateEntityException(String message) {
        super(message);
    }
}
