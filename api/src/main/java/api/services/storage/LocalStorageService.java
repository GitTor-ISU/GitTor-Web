package api.services.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import api.exceptions.StorageException;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link LocalStorageService}.
 */
@Slf4j
@Service
@Profile({"dev", "default"})
public class LocalStorageService implements SimpleStorageService {

    @Setter
    @Value("${s3.url:./temp/s3/}")
    private String rootPath;

    private Path root;

    /**
     * Initialize the local storage system.
     */
    @PostConstruct
    public void init() {
        root = Path.of(rootPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void uploadObject(String key, InputStream stream, long size) {
        validateKey(key);
        if (size < 0) {
            throw new IllegalArgumentException("Size must not be negative.");
        }

        Path target = root.resolve(key);
        try {
            byte[] data = stream.readNBytes((int) size);

            if (data.length < size) {
                throw new StorageException("unexpected EOF.");
            }

            Files.createDirectories(target.getParent());
            Files.write(target, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("S3 object uploaded: " + target.toAbsolutePath());
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public InputStream downloadObject(String key) {
        validateKey(key);
        Path target = root.resolve(key);
        if (!Files.exists(target)) {
            throw new StorageException("No file found for key: " + key);
        }
        try {
            return Files.newInputStream(target, StandardOpenOption.READ);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void deleteObject(String key) {
        validateKey(key);
        Path target = root.resolve(key);
        try {
            Files.deleteIfExists(target);
            log.info("S3 object deleted: " + target.toAbsolutePath());
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }
}
