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
    private EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Generate exception from user.
     *
     * @param username username
     * @return {@link EntityNotFoundException}
     */
    public static EntityNotFoundException fromUser(String username) {
        return new EntityNotFoundException(
            "User '" + username + "' not found."
        );
    }

    /**
     * Generate exception from user.
     *
     * @param id user id
     * @return {@link EntityNotFoundException}
     * @see EntityNotFoundException#fromUser(String) from username (when possible)
     */
    public static EntityNotFoundException fromUser(int id) {
        return new EntityNotFoundException("User " + id + " not found.");
    }

    /**
     * Generate exception from user avatar.
     *
     * @param username username
     * @return {@link EntityNotFoundException}
     */
    public static EntityNotFoundException fromUserAvatar(String username) {
        return new EntityNotFoundException(
            "User '" + username + "' avatar not found."
        );
    }

    /**
     * Generate exception from user avatar.
     *
     * @param id user id
     * @return {@link EntityNotFoundException}
     * @see EntityNotFoundException#fromUserAvatar(String) from username (when possible)
     */
    public static EntityNotFoundException fromUserAvatar(int id) {
        return new EntityNotFoundException("User " + id + " avatar not found.");
    }

    /**
     * Generate exception from role.
     *
     * @param role role name
     * @return {@link EntityNotFoundException}
     */
    public static EntityNotFoundException fromRole(String role) {
        return new EntityNotFoundException("Role '" + role + "' not found.");
    }

    /**
     * Generate exception from role.
     *
     * @param id role id
     * @return {@link EntityNotFoundException}
     * @see EntityNotFoundException#fromRole(String) from role name (when possible)
     */
    public static EntityNotFoundException fromRole(int id) {
        return new EntityNotFoundException("Role " + id + " not found.");
    }

    /**
     * Generate exception from authority.
     *
     * @param authority authority name
     * @return {@link EntityNotFoundException}
     */
    public static EntityNotFoundException fromAuthority(String authority) {
        return new EntityNotFoundException(
            "Authority '" + authority + "' not found."
        );
    }

    /**
     * Generate exception from authority.
     *
     * @param id authority id
     * @return {@link EntityNotFoundException}
     * @see EntityNotFoundException#fromAuthority(String) from authority name (when possible)
     */
    public static EntityNotFoundException fromAuthority(int id) {
        return new EntityNotFoundException("Authority " + id + " not found.");
    }

    /**
     * Generate exception from torrent.
     *
     * @param id torrent id
     * @return {@link EntityNotFoundException}
     */
    public static EntityNotFoundException fromTorrent(Long id) {
        return new EntityNotFoundException("Torrent " + id + " not found.");
    }

    /**
     * Generate exception from torrent.
     *
     * @param name torrent name
     * @return {@link EntityNotFoundException}
     */
    public static EntityNotFoundException fromTorrent(String name) {
        return new EntityNotFoundException("Torrent '" + name + "' not found.");
    }
}
