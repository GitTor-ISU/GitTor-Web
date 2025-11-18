package api.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import api.entities.MimeType;
import api.entities.S3Object;
import api.entities.Torrent;
import api.entities.User;
import api.exceptions.EntityNotFoundException;
import api.repositories.TorrentRepository;

/**
 * {@link TorrentService}.
 */
@Slf4j
@Service
public class TorrentService {

    @Autowired
    private S3ObjectService s3ObjectService;

    @Autowired
    private TorrentRepository torrentRepository;

    @Autowired
    private MimeTypeService mimeTypeService;

    public static final String TORRENT_DIR = "torrent/";
    private static final String TORRENT_MIME_TYPE = "application/x-bittorrent";

    @Value("${api.s3.torrent.max:10000000}")
    private long maxTorrentSize;

    /**
     * Find torrent by id.
     *
     * @param id Torrent id
     * @return {@link Optional} {@link Torrent}
     */
    @Transactional(readOnly = true)
    public Optional<Torrent> find(Long id) {
        return torrentRepository.findById(id);
    }

    /**
     * Find torrent by id with file eager loaded.
     *
     * @param id Torrent id
     * @return {@link Optional} {@link Torrent}
     */
    @Transactional(readOnly = true)
    public Optional<Torrent> findWithFile(Long id) {
        return torrentRepository.findByIdWithFile(id);
    }

    /**
     * Find torrent by name.
     *
     * @param name Torrent name
     * @return {@link Optional} {@link Torrent}
     */
    @Transactional(readOnly = true)
    public Optional<Torrent> find(String name) {
        return torrentRepository.findByName(name);
    }

    /**
     * Get torrent by id.
     *
     * @param id Torrent id
     * @return {@link Torrent}
     * @throws EntityNotFoundException if torrent not found
     */
    @Transactional(readOnly = true)
    public Torrent get(Long id) {
        return find(id).orElseThrow(() -> EntityNotFoundException.fromTorrent(id));
    }

    /**
     * Get torrent by id with file eager loaded.
     *
     * @param id Torrent id
     * @return {@link Torrent}
     * @throws EntityNotFoundException if torrent not found
     */
    @Transactional(readOnly = true)
    public Torrent getWithFile(Long id) {
        return findWithFile(id).orElseThrow(() -> EntityNotFoundException.fromTorrent(id));
    }

    /**
     * Get torrent by name.
     *
     * @param name Torrent name
     * @return {@link Torrent}
     * @throws EntityNotFoundException if torrent not found
     */
    @Transactional(readOnly = true)
    public Torrent get(String name) {
        return find(name).orElseThrow(() -> EntityNotFoundException.fromTorrent(name));
    }

    /**
     * Get all torrents.
     *
     * @param pageable {@link Pageable}
     * @return {@link Page} of {@link Torrent}
     */
    @Transactional(readOnly = true)
    public Page<Torrent> getAll(Pageable pageable) {
        return torrentRepository.findAll(pageable);
    }

    /**
     * Get all torrents by uploader.
     *
     * @param uploader Uploader
     * @param pageable {@link Pageable}
     * @return {@link Page} of {@link Torrent}
     */
    @Transactional(readOnly = true)
    public Page<Torrent> getAllByUploader(User uploader, Pageable pageable) {
        return torrentRepository.findAllByUploader(uploader, pageable);
    }

    /**
     * Check if torrent exists.
     *
     * @param id Torrent id
     * @return true if exists
     */
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return torrentRepository.existsById(id);
    }

    /**
     * Save torrent.
     *
     * @param torrent Torrent
     * @return {@link Torrent}
     */
    @Transactional
    public Torrent save(Torrent torrent) {
        Torrent saved = torrentRepository.save(torrent);
        log.info("Torrent saved: " + torrent);
        return saved;
    }

    /**
     * Create new torrent.
     *
     * @param name Torrent name
     * @param description Torrent description
     * @param uploader Uploader
     * @param file Torrent file
     * @return {@link Torrent}
     * @throws IOException if file cannot be read
     */
    @Transactional
    public Torrent create(String name, String description, User uploader, MultipartFile file) throws IOException {
        validateTorrentFile(file);

        MimeType mimeType = mimeTypeService.getOrCreateByName(TORRENT_MIME_TYPE);
        S3Object s3Object =
            S3Object.builder().key(TORRENT_DIR + UUID.randomUUID()).size(file.getSize()).mimeType(mimeType).build();

        s3ObjectService.save(s3Object, file.getInputStream());

        Torrent torrent =
            Torrent.builder().name(name).description(description).file(s3Object).uploader(uploader).build();

        return save(torrent);
    }

    /**
     * Update torrent metadata.
     *
     * @param id Torrent id
     * @param name New name (optional)
     * @param description New description (optional)
     * @return {@link Torrent}
     */
    @Transactional
    public Torrent updateMetadata(Long id, String name, String description) {
        Torrent torrent = get(id);

        if (name != null && !name.isBlank()) {
            torrent.setName(name);
        }
        if (description != null) {
            torrent.setDescription(description);
        }

        return save(torrent);
    }

    /**
     * Update torrent file.
     *
     * @param id Torrent id
     * @param file New torrent file
     * @throws IOException if file cannot be read
     */
    @Transactional
    public void updateTorrentFile(Long id, MultipartFile file) throws IOException {
        Torrent torrent = get(id);
        validateTorrentFile(file);

        S3Object oldFile = torrent.getFile();

        MimeType mimeType = mimeTypeService.getOrCreateByName(TORRENT_MIME_TYPE);
        S3Object newFile =
            S3Object.builder().key(TORRENT_DIR + UUID.randomUUID()).size(file.getSize()).mimeType(mimeType).build();

        s3ObjectService.save(newFile, file.getInputStream());
        torrent.setFile(newFile);
        save(torrent);

        // Delete old file after successfully saving new one
        if (oldFile != null) {
            s3ObjectService.delete(oldFile);
        }
    }

    /**
     * Download torrent file.
     *
     * @param id Torrent id
     * @return {@link InputStream} of torrent file
     */
    @Transactional(readOnly = true)
    public InputStream downloadTorrentFile(Long id) {
        Torrent torrent = getWithFile(id);
        S3Object file = torrent.getFile();
        if (file == null) {
            throw new IllegalStateException("Torrent " + id + " has no file.");
        }
        return s3ObjectService.download(file);
    }

    /**
     * Delete torrent.
     *
     * @param id Torrent id
     */
    @Transactional
    public void delete(Long id) {
        Torrent torrent = get(id);
        torrentRepository.delete(torrent);
        // S3Object will be deleted automatically due to orphanRemoval = true
        log.info("Torrent deleted: " + id);
    }

    /**
     * Validate torrent file.
     *
     * @param file File to validate
     * @throws IllegalArgumentException if file is invalid
     */
    private void validateTorrentFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty.");
        }
        if (file.getSize() > maxTorrentSize) {
            throw new IllegalArgumentException("File size exceeds limit (" + maxTorrentSize + " bytes).");
        }
        String contentType = file.getContentType();
        if (!TORRENT_MIME_TYPE.equals(contentType)) {
            throw new IllegalArgumentException("Only torrent files (application/x-bittorrent) are allowed.");
        }
    }
}
