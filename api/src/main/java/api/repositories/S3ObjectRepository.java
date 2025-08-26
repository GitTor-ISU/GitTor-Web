package api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import api.entities.S3Object;

/**
 * {@link S3ObjectRepository}.
 */
@Repository
public interface S3ObjectRepository extends JpaRepository<S3Object, Integer> {
}
