package api.services;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import api.dtos.AuthenticationDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

/**
 * {@link TokenService}.
 */
@Service
public class TokenService {
    @Autowired
    private Clock clock;

    @SuppressWarnings("checkstyle:LineLength")
    @Value("${jwt.token.secret:XrLHWXPiznJfz3jvJF9ZJkIzvgC0RAF64dOO8bqSxJ2LStAOUAIO85gWg7tcFlfLL9c6q40UCRKMlwnyM5OQOg==}")
    private String secret;

    @Value("${jwt.token.expires-minutes:1440}")
    private int expiresMinutes;

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
     * @param authentication User authentication
     * @return {@link AuthenticationDto}
     */
    public AuthenticationDto generateToken(Authentication authentication) {
        String username = authentication.getName();
        Instant now = Instant.now(clock);
        Date issuedAt = Date.from(now);
        Date expireDate = Date.from(now.plus(expiresMinutes, ChronoUnit.MINUTES));

        String token = Jwts.builder()
            .subject(username)
            .issuedAt(issuedAt)
            .expiration(expireDate)
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
            .compact();

        return new AuthenticationDto(token, "Bearer", expireDate);
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
            throw new AuthenticationCredentialsNotFoundException("Token is expired");
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("Token is invalid");
        }
    }
}
