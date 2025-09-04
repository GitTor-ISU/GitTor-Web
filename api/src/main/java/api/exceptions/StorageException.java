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
    private StorageException(String message) {
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

    /**
     * Generate exception from unexpected EOF.
     *
     * @return {@link StorageException}
     */
    public static StorageException fromEndOfFile() {
        return new StorageException("Unexpected EOF.");
    }

    /**
     * Generate exception from file not found.
     *
     * @param key S3 key
     * @return {@link StorageException}
     */
    public static StorageException fromNotFound(String key) {
        return new StorageException("No file found for key: " + key);
    }
}
