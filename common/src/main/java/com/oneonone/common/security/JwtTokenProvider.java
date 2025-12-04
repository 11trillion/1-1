package com.oneonone.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j
public class JwtTokenProvider {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_KEY = "auth";
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.token.expiration}")
    private long expiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    private SecretKey key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        log.info("Initializing JWT Token Provider");
        if (secretKey == null || secretKey.isEmpty())
            throw new IllegalArgumentException("JWT secret key can not be null or empty");
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
        log.info("JWT Token Provider initialized successful");
    }

    public String createToken(Long userId, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + expiration);

        return BEARER_PREFIX + Jwts.builder()
                .setSubject(userId.toString())
                .claim(AUTHORIZATION_KEY, role)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshExpiration);

        return BEARER_PREFIX + Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    public String substringToken(String token) {
        if (token != null && token.startsWith(BEARER_PREFIX)) return token.substring(BEARER_PREFIX.length());
        throw new IllegalArgumentException("Invalid token format");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get(AUTHORIZATION_KEY, String.class);
    }
}
