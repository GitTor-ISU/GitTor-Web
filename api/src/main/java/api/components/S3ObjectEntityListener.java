package api.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import api.entities.S3Object;
import api.services.storage.SimpleStorageService;
import jakarta.persistence.PreRemove;

/**
 * {@link S3ObjectEntityListener}.
 */
@Component
public class S3ObjectEntityListener {
    @Autowired
    private SimpleStorageService simpleStorageService;

    /**
     * Before any {@link S3ObjectEntity} is removed from the database, remove it from the S3 storage.
     *
     * @param object Object
     */
    @PreRemove
    public void onPreRemove(S3Object object) {
        simpleStorageService.deleteObject(object.getKey());
    }
}
