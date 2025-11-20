package io.github.jaredmcc4.gtm.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key getSigningKey() {
        byte[] keyBytes = decodeSecret(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret(String rawSecret) {
        if (rawSecret == null || rawSecret.isBlank()) {
            throw new IllegalArgumentException("La propiedad jwt.secret no puede ser vacia");
        }

        try {
            return Decoders.BASE64.decode(rawSecret);
        } catch (IllegalArgumentException | DecodingException ignored) {
            // Intenta siguiente estrategia
        }

        try {
            return Decoders.BASE64URL.decode(rawSecret);
        } catch (IllegalArgumentException | DecodingException ignored) {
            // Fallback final a texto plano
        }

        return rawSecret.getBytes(StandardCharsets.UTF_8);
    }

    public String generarToken(String email, Long usuarioId, List<String> roles) {
        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + expiration);

        return Jwts.builder()
                .setSubject(email)
                .claim("usuarioId", usuarioId)
                .claim("roles", roles)
                .setIssuedAt(issuedAt)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validarToken(String token, String email) {
        try {
            Claims claims = extraerClaims(token);
            return email.equals(claims.getSubject()) && !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extraerExpiration(token).before(new Date());
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Date extraerExpiration(String token) {
        return extraerClaims(token).getExpiration();
    }

    public String extraerEmail(String token) {
        String subject = extraerClaims(token).getSubject();
        return subject == null ? "" : subject;
    }

    public Long extraerUsuarioId(String token) {
        Object usuarioIdClaim = extraerClaims(token).get("usuarioId");
        if (usuarioIdClaim instanceof Long) {
            return (Long) usuarioIdClaim;
        } else if (usuarioIdClaim instanceof Integer) {
            return ((Integer) usuarioIdClaim).longValue();
        } else if (usuarioIdClaim instanceof String) {
            return Long.parseLong((String) usuarioIdClaim);
        } else if (usuarioIdClaim instanceof Number) {
            return ((Number) usuarioIdClaim).longValue();
        }
        throw new IllegalArgumentException("Formato de usuarioId no soportado");
    }

    @SuppressWarnings("unchecked")
    public List<String> extraerRoles(String token) {
        Object roles = extraerClaims(token).get("roles");
        if (roles instanceof List<?> lista) {
            return lista.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }
}
