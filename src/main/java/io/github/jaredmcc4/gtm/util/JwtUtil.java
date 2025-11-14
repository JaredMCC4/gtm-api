package io.github.jaredmcc4.gtm.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(String email, Long usuarioId, List<String> roles) {
        return Jwts.builder()
                .subject(email)
                .claim("usuarioId", usuarioId)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    public Long extraerUsuarioId(String token) {
        return extraerClaims(token).get("usuarioId", Long.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extraerRoles(String token) {
        return extraerClaims(token).get("roles", List.class);
    }

    public boolean validarToken(String token, String email) {
        String emailToken = extraerEmail(token);
        return emailToken.equals(email) && !isTokenExpirado(token);
    }

    private boolean isTokenExpirado(String token) {
        return extraerClaims(token).getExpiration().before(new Date());
    }
}
