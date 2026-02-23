package api.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import api.entities.Torrent;
import api.entities.User;

/**
 * {@link TorrentRepository}.
 */
@Repository
public interface TorrentRepository extends JpaRepository<Torrent, Long> {
    /**
     * Find torrent by name.
     *
     * @param name Torrent name
     * @return {@link Optional} {@link Torrent}
     */
    Optional<Torrent> findByName(String name);

    /**
     * Find all torrents by uploader.
     *
     * @param uploader Uploader
     * @param pageable {@link Pageable}
     * @return {@link Page} of {@link Torrent}
     */
    Page<Torrent> findAllByUploader(User uploader, Pageable pageable);
}
