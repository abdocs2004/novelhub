package com.novelhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Creates and verifies JWTs.
 * A JWT is "header.payload.signature". The payload holds claims (subject = email, role, expiry).
 * The signature is made with our secret key, so nobody can change the payload without the server noticing.
 * The payload is only Base64-encoded, NOT encrypted: never put secrets in it.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // Throws at startup if the secret is shorter than 32 bytes (fail fast).
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserPrincipal user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())          // "sub" claim = email
                .claim("role", user.getRole().name()) // informational only: the real role is re-read from the DB
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)                        // HS256 because the key is 32 bytes
                .compact();
    }

    /**
     * Verifies signature + expiry and returns the email.
     * Throws io.jsonwebtoken.JwtException if the token is invalid, tampered with, or expired.
     */
    public String extractEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}
