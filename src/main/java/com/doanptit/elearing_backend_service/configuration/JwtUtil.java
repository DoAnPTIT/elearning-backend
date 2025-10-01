package com.doanptit.elearing_backend_service.configuration;

import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
@Getter
@Setter
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKey123456789012345678901234567890}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private static final String CLAIM_ROLE = "role";

    // Tạo token
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_ROLE, role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    // Lấy username từ token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Lấy role từ token
    public String extractRole(String token) {
        return (String) extractAllClaims(token).get(CLAIM_ROLE);
    }

    // Validate token so khớp với username
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Check token hết hạn
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}
