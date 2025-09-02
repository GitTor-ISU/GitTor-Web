package api.services.storage;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import api.exceptions.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link MinioStorageService}.
 */
@Slf4j
@Service
@Profile("prod")
public class MinioStorageService implements SimpleStorageService {
    @Autowired
    private MinioClient minioClient;

    @Value("${s3.bucket.name:main}")
    private String defaultBucket;

    /**
     * Initialize the Minio storage system.
     */
    @PostConstruct
    public void init() {
        createBucket(defaultBucket);
    }

    @Override
    public void uploadObject(String key, InputStream stream, long size) {
        uploadObject(defaultBucket, key, stream, size);
    }

    private void uploadObject(String bucket, String key, InputStream stream, long size) {
        validateKey(key);
        if (size < 0) {
            throw new IllegalArgumentException("Size must not be negative.");
        }

        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(stream, size, -1)
                    .build()
            );
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            throw new StorageException(e);
        }
        log.info("S3 object uploaded: " + bucket + "/" + key);
    }

    @Override
    public InputStream downloadObject(String key) {
        return downloadObject(defaultBucket, key);
    }

    private InputStream downloadObject(String bucket, String key) {
        validateKey(key);
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .build()
            );
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void deleteObject(String key) {
        deleteObject(defaultBucket, key);
    }

    private void deleteObject(String bucket, String key) {
        validateKey(key);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .build()
            );
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            throw new StorageException(e);
        }
        log.info("S3 object deleted: " + bucket + "/" + key);
    }

    /**
     * Create a new Minio bucket.
     *
     * @param bucket bucket name
     */
    public void createBucket(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucket).build()
                );
                log.info("S3 bucket created: " + bucket);
            }
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
                | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
                | IllegalArgumentException | IOException e) {
            throw new StorageException(e);
        }
    }
}
