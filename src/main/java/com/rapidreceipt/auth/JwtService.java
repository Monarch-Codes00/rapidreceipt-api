package com.rapidreceipt.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles all JWT operations: token generation, parsing, and validation.
 *
 * Key design decisions:
 * - Secret is read from app.jwt.secret in application.yml, which reads from
 *   the JWT_SECRET environment variable (with a long dev default).
 * - Keys.hmacShaKeyFor(bytes) is used instead of BASE64 decoding — the secret
 *   can be any plain string >= 32 characters (256 bits) for HS256 to be valid.
 * - The token subject is the user's email address (our unique login identifier).
 * - Extra claims map allows future callers to embed additional data (e.g. roles)
 *   into the token payload without changing this class.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generates a token with no extra claims — just the user's email as subject.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a token with optional extra claims embedded in the payload.
     * extraClaims can be empty — included for future extensibility.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())          // email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the email (subject claim) from a token.
     * Used by JwtAuthenticationFilter to identify the user.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Returns true if the token is signed correctly AND belongs to this user
     * AND is not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Builds the signing key from the raw secret string bytes.
     * The secret must be at least 32 characters (256 bits) for HS256.
     * Using getBytes(UTF_8) instead of BASE64 decoding avoids format requirements
     * on the secret value — any long random string works as a dev secret.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
