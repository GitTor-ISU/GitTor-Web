package api.services.storage;

import java.io.InputStream;

/**
 * {@link SimpleStorageService}.
 */
public interface SimpleStorageService {
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
