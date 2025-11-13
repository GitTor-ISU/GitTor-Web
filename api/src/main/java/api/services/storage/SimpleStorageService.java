package api.services.storage;

import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * {@link SimpleStorageService}.
 */
public interface SimpleStorageService {
    Pattern VALID_KEY = Pattern.compile("^(?!/)(?!.*\\./)[a-zA-Z0-9_./\\-]+$");

    /**
     * Validate key matches required pattern.
     *
     * @param key S3 key
     */
    default void validateKey(String key) {
        if (key == null || !VALID_KEY.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid key: '" + key + "'. " + VALID_KEY.pattern());
        }
    }

    /**
     * Upload object to the storage system.
     *
     * @param key The identifier for the object
     * @param stream The object content stream
     * @param size The size of the object content in bytes
     */
    public void uploadObject(String key, InputStream stream, long size);

    /**
     * Download object from the storage system.
     *
     * @param key The identifier for the object
     * @return The object content stream
     */
    public InputStream downloadObject(String key);

    /**
     * Delete object from the storage system.
     *
     * @param key The identifier for the object
     */
    public void deleteObject(String key);
}
