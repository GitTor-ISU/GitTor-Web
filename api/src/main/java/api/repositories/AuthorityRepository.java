package api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import api.entities.Authority;

/**
 * {@link RoleRepository}.
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {
    /**
     * Find authority object by authority name.
     *
     * @param authority authority name
     * @return {@link Optional} {@link Authority}
     */
    public Optional<Authority> findByAuthority(String authority);
}
