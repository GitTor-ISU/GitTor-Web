package api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import api.entities.RefreshToken;
import api.entities.User;

/**
 * {@link RefreshTokenRepository}.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    /**
     * Find refresh token.
     *
     * @param token Refresh token
     * @return {@link Optional} {@link RefreshToken}
     */
    public Optional<RefreshToken> findByToken(String token);


    /**
     * Find refresh token by user.
     *
     * @param user User
     * @return {@link Optional} {@link RefreshToken}
     */
    public Optional<RefreshToken> findByUser(User user);
}
