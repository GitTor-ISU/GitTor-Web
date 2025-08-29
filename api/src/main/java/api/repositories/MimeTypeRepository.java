package api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import api.entities.MimeType;

/**
 * {@link MimeTypeRepository}.
 */
@Repository
public interface MimeTypeRepository extends JpaRepository<MimeType, Integer> {
    /**
     * Find mime type object by name.
     *
     * @param name mime type name
     * @return {@link Optional} {@link MimeType}
     */
    public Optional<MimeType> findByName(String name);
}
