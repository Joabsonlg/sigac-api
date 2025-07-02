package io.github.joabsonlg.sigac_api.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for JWT token operations.
 * Handles token creation, validation, and extraction of claims.
 */
@Service
public class JwtService {
    
    @Value("${sigac.jwt.secret:sigac-secret-key-for-jwt-token-generation-and-validation-very-secure}")
    private String secretKey;
    
    @Value("${sigac.jwt.access-token-expiration:3600}")
    private Long accessTokenExpiration; // 1 hour
    
    @Value("${sigac.jwt.refresh-token-expiration:604800}")
    private Long refreshTokenExpiration; // 7 days
    
    /**
     * Generates an access token for the given user.
     *
     * @param cpf  user's CPF
     * @param role user's role
     * @return JWT access token
     */
    public String generateAccessToken(String cpf, String role) {
        return generateToken(cpf, role, accessTokenExpiration, "access");
    }
    
    /**
     * Generates a refresh token for the given user.
     *
     * @param cpf user's CPF
     * @return JWT refresh token
     */
    public String generateRefreshToken(String cpf) {
        return generateToken(cpf, null, refreshTokenExpiration, "refresh");
    }
    
    /**
     * Validates a JWT token.
     *
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extracts CPF from JWT token.
     *
     * @param token JWT token
     * @return user's CPF
     */
    public String extractCpf(String token) {
        return getClaims(token).getSubject();
    }
    
    /**
     * Extracts role from JWT token.
     *
     * @param token JWT token
     * @return user's role
     */
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }
    
    /**
     * Extracts token type from JWT token.
     *
     * @param token JWT token
     * @return token type (access or refresh)
     */
    public String extractTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }
    
    /**
     * Checks if token is expired.
     *
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
    
    /**
     * Gets expiration time of token in seconds.
     *
     * @param tokenType type of token (access or refresh)
     * @return expiration time in seconds
     */
    public Long getExpirationTime(String tokenType) {
        return "refresh".equals(tokenType) ? refreshTokenExpiration : accessTokenExpiration;
    }
    
    /**
     * Generates JWT token with specified parameters.
     */
    private String generateToken(String cpf, String role, Long expiration, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        if (role != null) {
            claims.put("role", role);
        }
        claims.put("type", tokenType);
        
        Instant now = Instant.now();
        Instant expirationTime = now.plus(expiration, ChronoUnit.SECONDS);
        
        return Jwts.builder()
                .claims(claims)
                .subject(cpf)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Gets claims from JWT token.
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Gets signing key for JWT operations.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
