package api.repositories;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import api.entities.User;

/**
 * {@link UserRepository}.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * Find user by username.
     *
     * @param username username
     * @return {@link Optional} {@link User}
     */
    public Optional<User> findByUsername(String username);

    /**
     * Find user by username and load authorities.
     *
     * @param username username
     * @return {@link Optional} {@link User}
     */
    @Query("""
        SELECT u FROM User u
        LEFT JOIN FETCH u.roles r
        LEFT JOIN FETCH r.authorities
        WHERE u.username = :username
        """)
    public Optional<User> findByUsernameWithAuthorities(@Param("username") String username);

    /**
     * Find all user ids.
     *
     * @return User ids
     */
    @Query("SELECT u.id FROM User u")
    public Set<Integer> findAllUserIds();
}
