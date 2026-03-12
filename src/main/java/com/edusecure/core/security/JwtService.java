package com.edusecure.core.security;


import com.edusecure.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // ─── Génération des tokens ───────────────────────────────────────

    public String generateAccessToken(UserDetails userDetails,
                                      String tenantId) {
        Map<String, Object> claims = new HashMap<>();

        // On stocke le tenantId dans le token
        // → sécurité renforcée : le tenant vient du JWT, pas du header
        claims.put("tenantId", tenantId);
        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList());

        return buildToken(claims, userDetails.getUsername(),
                jwtProperties.getExpiration());
    }

    public String generateRefreshToken(UserDetails userDetails, String tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenantId);
        claims.put("type", "refresh");

        return buildToken(claims, userDetails.getUsername(),
                jwtProperties.getRefreshExpiration());
    }

    private String buildToken(Map<String, Object> extraClaims, String subject,long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ─── Validation ──────────────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ─── Extraction des claims ───────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTenantId(String token) {
        return extractClaim(token,
                claims -> claims.get("tenantId", String.class));
    }

    public <T> T extractClaim(String token,Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    // ─── Clé de signature ────────────────────────────────────────────

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}