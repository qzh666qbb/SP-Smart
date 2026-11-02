package com.example.training.auth.domain.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    
    @Value("${app.jwt.access-token-expire-seconds}")
    private int accessTokenExpireSeconds;
    
    @Value("${app.jwt.refresh-token-expire-seconds}")
    private int refreshTokenExpireSeconds;
    
    @Value("${app.jwt.secret}")
    private String secret;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateAccessToken(Long userId, String username, String role, String phone, Long profileId, String profileType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("tokenType", "access");
        if (phone != null) claims.put("phone", phone);
        if (profileId != null) claims.put("profileId", profileId);
        if (profileType != null) claims.put("profileType", profileType);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(accessTokenExpireSeconds)))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractPhone(String token) {
        Claims claims = parseToken(token);
        return claims.get("phone", String.class);
    }

    public Long extractProfileId(String token) {
        Claims claims = parseToken(token);
        Object v = claims.get("profileId");
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.valueOf(v.toString()); } catch (Exception e) { return null; }
    }

    public String extractProfileType(String token) {
        Claims claims = parseToken(token);
        return claims.get("profileType", String.class);
    }
    
    public String generateRefreshToken(Long userId, String username, int tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "refresh");
        claims.put("tokenVersion", tokenVersion);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(refreshTokenExpireSeconds)))
                .signWith(getSigningKey())
                .compact();
    }
    
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    public Long extractUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
    
    public String extractUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
    
    public String extractRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(parseToken(token).get("tokenType", String.class));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseToken(token).get("tokenType", String.class));
    }

    public int extractTokenVersion(String token) {
        Object value = parseToken(token).get("tokenVersion");
        if (value == null) return 0;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(value.toString());
    }
    
    public LocalDateTime getExpirationTime(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
