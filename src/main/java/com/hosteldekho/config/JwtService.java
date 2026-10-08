package com.hosteldekho.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    String secret;
    @Value("${app.jwt.expiration-ms}")
    long expiration;

    @PostConstruct
    void validateSecret() {
        key();
    }

    private SecretKey key() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 characters");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 characters");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String create(String email) {
        return Jwts.builder().subject(email).issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration)).signWith(key()).compact();
    }

    public String email(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject();
    }
}
