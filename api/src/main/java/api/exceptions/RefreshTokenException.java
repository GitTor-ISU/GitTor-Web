package api.exceptions;

/**
 * {@link RefreshTokenException}.
 */
public class RefreshTokenException extends RuntimeException {
    /**
     * RefreshTokenException.
     */
    public RefreshTokenException() {
        super("Refresh token is invalid.");
    }

    /**
     * RefreshTokenException.
     *
     * @param cause Original exception
     */
    public RefreshTokenException(Throwable cause) {
        super(cause);
    }
}
