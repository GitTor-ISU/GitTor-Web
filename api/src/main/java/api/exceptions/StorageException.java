package api.exceptions;

/**
 * {@link StorageException}.
 */
public class StorageException extends RuntimeException {
    /**
     * StorageException.
     *
     * @param message Error message
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * StorageException.
     *
     * @param cause Original exception
     */
    public StorageException(Throwable cause) {
        super(cause);
    }
}
