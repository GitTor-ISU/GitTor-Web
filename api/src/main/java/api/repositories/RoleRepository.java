package api.repositories;

import java.util.Optional;

import api.entities.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link RoleRepository}.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    /**
     * Find role object by role name.
     *
     * @param name role name
     * @return {@link Optional} {@link Role}
     */
    public Optional<Role> findByName(String name);
}
