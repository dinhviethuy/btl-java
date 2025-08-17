package com.fullnestjob.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.access-token-expiration}")
    private Duration accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    private Key getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e1) {
            try {
                keyBytes = Decoders.BASE64URL.decode(secret);
            } catch (IllegalArgumentException e2) {
                keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 256 bits (32 bytes). Current: " + (keyBytes.length * 8) + " bits");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public javax.crypto.SecretKey getSigningKeyForVerify() {
        return (javax.crypto.SecretKey) getSigningKey();
    }

    public String generateAccessToken(Map<String, Object> claims) {
        return generateToken(claims, accessTokenExpiration.toMillis());
    }

    public String generateRefreshToken(Map<String, Object> claims) {
        return generateToken(claims, refreshTokenExpiration.toMillis());
    }

    private String generateToken(Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .subject((String) claims.getOrDefault("sub", ""))
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Date getExpiration(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(getSigningKeyForVerify())
                    .build().parseSignedClaims(token);
            return claims.getPayload().getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    public int getRefreshTokenMaxAgeSeconds() {
        return (int) Math.max(0, refreshTokenExpiration.getSeconds());
    }
}


