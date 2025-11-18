package api.exceptions;

/**
 * {@link RefreshTokenException}.
 */
public class RefreshTokenException extends RuntimeException {
    /**
     * RefreshTokenException.
     */
    public RefreshTokenException() {
        super("Login has expired.");
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
