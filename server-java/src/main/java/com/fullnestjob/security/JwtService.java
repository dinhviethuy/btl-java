package com.fullnestjob.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public javax.crypto.SecretKey getSigningKeyForVerify() {
        return (javax.crypto.SecretKey) getSigningKey();
    }

    public String generateAccessToken(Map<String, Object> claims) {
        return generateToken(claims, accessTokenExpirationMs);
    }

    public String generateRefreshToken(Map<String, Object> claims) {
        return generateToken(claims, refreshTokenExpirationMs);
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
}


