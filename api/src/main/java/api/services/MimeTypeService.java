package api.services;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api.entities.MimeType;
import api.repositories.MimeTypeRepository;

/**
 * {@link MimeTypeService}.
 */
@Slf4j
@Service
public class MimeTypeService {
    @Autowired
    private MimeTypeRepository mimeTypeRepository;

    /**
     * Find mime type.
     *
     * @param name mime type name
     * @return {@link Optional} {@link MimeType}
     */
    @Transactional(readOnly = true)
    public Optional<MimeType> find(String name) {
        return mimeTypeRepository.findByName(name);
    }

    /**
     * Save mime type.
     *
     * @param mimeType mime type
     * @return {@link MimeType}
     */
    @Transactional
    public MimeType save(MimeType mimeType) {
        log.info("Mime type saved: " + mimeType);
        return mimeTypeRepository.save(mimeType);
    }

    /**
     * Gets mime type by name and creates it if non-existent.
     *
     * @param name mime type name
     * @return {@link MimeType}
     */
    @Transactional
    public MimeType getOrCreateByName(String name) {
        return find(name).orElseGet(() -> save(MimeType.builder().name(name).build()));
    }
}
