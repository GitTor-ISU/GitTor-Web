package api.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Find torrent by id and eager load file.
     *
     * @param id Torrent id
     * @return {@link Optional} {@link Torrent}
     */
    @Query("""
        SELECT t FROM Torrent t
        LEFT JOIN FETCH t.file
        WHERE t.id = :id
        """)
    Optional<Torrent> findByIdWithFile(@Param("id") Long id);
}
