package api.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

import api.dtos.AuthenticationDto;
import api.entities.RefreshToken;
import api.entities.User;
import api.exceptions.RefreshTokenException;
import api.repositories.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link TokenService}.
 */
@Slf4j
@Service
public class TokenService {
    @Autowired
    private Clock clock;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @SuppressWarnings("checkstyle:LineLength")
    @Value("${jwt.token.secret:XrLHWXPiznJfz3jvJF9ZJkIzvgC0RAF64dOO8bqSxJ2LStAOUAIO85gWg7tcFlfLL9c6q40UCRKMlwnyM5OQOg==}")
    private String secret;

    @Value("${jwt.token.expires-minutes:30}")
    private int accessTokenExpiresMinutes;

    @Value("${jwt.access-token.expires-days:7}")
    private int refreshTokenExpiresDays;

    private JwtParser parser;

    /**
     * Initialize parser.
     */
    @PostConstruct
    public void init() {
        parser = Jwts.parser()
            .clock(() -> Date.from(Instant.now(clock)))
            .verifyWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
            .build();
    }

    /**
     * Generate new JWT token.
     *
     * @param username Username
     * @return {@link AuthenticationDto}
     */
    @Transactional
    public AuthenticationDto generateAccessToken(String username) {
        Instant now = Instant.now(clock);
        Date issuedAt = Date.from(now);
        Date expireDate = Date.from(now.plus(accessTokenExpiresMinutes, ChronoUnit.MINUTES));

        String accessToken = Jwts.builder()
            .subject(username)
            .issuedAt(issuedAt)
            .expiration(expireDate)
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
            .compact();
        return new AuthenticationDto(accessToken, "Bearer", expireDate);
    }

    /**
     * Get or generate a refresh token for a user.
     *
     * @param user User info
     * @return {@link AuthenticationDto}
     */
    @Transactional
    public RefreshToken generateRefreshToken(User user) {
        return refreshTokenRepository.findByUser(user)
            .map(this::regenerateRefreshToken)
            .orElseGet(() -> {
                String rawToken = UUID.randomUUID().toString();
                String hashed = hash(rawToken);

                RefreshToken newToken = RefreshToken.builder()
                    .user(user)
                    .expires(Instant.now(clock).plus(refreshTokenExpiresDays, ChronoUnit.DAYS))
                    .token(hashed)
                    .rawToken(rawToken)
                    .build();

                return refreshTokenRepository.save(newToken);
            });
    }

    /**
     * Validates a refresh token.
     *
     * @param refreshToken Refresh token value
     * @return {@link String} username
     */
    @Transactional
    public RefreshToken validateAndRegenerate(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(hash(refreshToken))
            .orElseThrow(() -> new RefreshTokenException());

        if (token.getExpires().isBefore(Instant.now(clock))) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException();
        }

        RefreshToken regen = regenerateRefreshToken(token);
        regen.getUser().getUsername();

        return regen;
    }

    /**
     * Invalidates a refresh token.
     *
     * @param refreshToken Refresh token value
     */
    @Transactional
    public void invalidate(String refreshToken) {
        refreshTokenRepository.findByToken(hash(refreshToken))
            .ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Get username from JWT token.
     *
     * @param token JWT token
     * @return Username
     */
    public String getUsernameByToken(String token) {
        try {
            return parser.parseSignedClaims(token).getPayload().getSubject();
        } catch (ExpiredJwtException e) {
            throw new AuthenticationCredentialsNotFoundException("Access token is expired");
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("Access token is invalid");
        }
    }

    @Transactional
    private RefreshToken regenerateRefreshToken(RefreshToken token) {
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hash(rawToken);

        token.setExpires(Instant.now(clock).plus(refreshTokenExpiresDays, ChronoUnit.DAYS));
        token.setToken(hashedToken);
        token.setRawToken(rawToken);

        return refreshTokenRepository.save(token);
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
