package api.services;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api.entities.S3Object;
import api.repositories.S3ObjectRepository;
import api.services.storage.SimpleStorageService;

/**
 * {@link S3ObjectService} is the connection to the S3 storage and S3 object database.
 */
@Service
public class S3ObjectService {
    @Autowired
    private S3ObjectRepository s3ObjectRepository;
    @Autowired
    private SimpleStorageService simpleStorageService;

    public static final String AVATAR_DIR = "avatar/";

    /**
     * Download S3 object from storage.
     *
     * @param object Object
     * @return {@link InputStream}
     */
    @Transactional(readOnly = true)
    public InputStream download(S3Object object) {
        return simpleStorageService.downloadObject(object.getKey());
    }

    /**
     * Save object to S3 storage and S3 object database.
     *
     * @param object Object
     * @param in File to save
     */
    @Transactional
    public void save(S3Object object, InputStream in) {
        simpleStorageService.uploadObject(object.getKey(), in, object.getSize());
        s3ObjectRepository.save(object);
    }

    /**
     * Delete object from S3 storage and S3 object database.
     *
     * @param object Object
     */
    @Transactional
    public void delete(S3Object object) {
        s3ObjectRepository.delete(object);
        simpleStorageService.deleteObject(object.getKey());
    }
}
