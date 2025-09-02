package api.services.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import api.exceptions.StorageException;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link MemoryStorageService}.
 */
@Slf4j
@Service
@Profile("test")
public class MemoryStorageService implements SimpleStorageService {
    private final Map<String, byte[]> storage = new ConcurrentHashMap<>();

    @Override
    public void uploadObject(String key, InputStream stream, long size) {
        validateKey(key);
        if (size < 0) {
            throw new IllegalArgumentException("Size must not be negative.");
        }

        try {
            byte[] data = stream.readNBytes((int) size);

            if (data.length < size) {
                throw new StorageException("unexpected EOF.");
            }

            storage.put(key, data);
            log.info("S3 object uploaded: " + key);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public InputStream downloadObject(String key) {
        validateKey(key);
        byte[] data = storage.get(key);
        if (data == null) {
            throw new StorageException("No file found for key: " + key);
        }
        return new ByteArrayInputStream(data);
    }

    @Override
    public void deleteObject(String key) {
        validateKey(key);
        storage.remove(key);
        log.info("S3 object deleted: " + key);
    }
}
