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
    private DuplicateEntityException(String message) {
        super(message);
    }

    /**
     * Generate exception from user.
     *
     * @param username username
     * @return {@link DuplicateEntityException}
     */
    public static DuplicateEntityException fromUsername(String username) {
        return new DuplicateEntityException("User '" + username + "' already exists.");
    }

    /**
     * Generate exception from email.
     *
     * @param email email
     * @return {@link DuplicateEntityException}
     */
    public static DuplicateEntityException fromEmail(String email) {
        return new DuplicateEntityException("Email '" + email + "' already in use.");
    }

    /**
     * Generate exception from role.
     *
     * @param role role name
     * @return {@link DuplicateEntityException}
     */
    public static DuplicateEntityException fromRole(String role) {
        return new DuplicateEntityException("Role '" + role + "' already exists.");
    }
}
