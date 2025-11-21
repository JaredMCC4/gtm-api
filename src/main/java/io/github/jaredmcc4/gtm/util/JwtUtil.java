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

    /**
     * Utilidades para generar y validar JWT HMAC-SHA256, asi como extraer claims
     * propios del dominio (usuarioId, roles, email). Acepta secretos en Base64,
     * Base64URL o texto plano como ultima alternativa.
     */
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Construye la llave criptografica a partir del secreto configurado.
     *
     * @return llave HMAC-SHA necesaria por JJWT
     */
    private Key getSigningKey() {
        byte[] keyBytes = decodeSecret(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Decodifica el secreto aceptando Base64, Base64URL o devolviendo los bytes
     * del texto plano cuando no se pudo decodificar.
     *
     * @param rawSecret secreto configurado
     * @return bytes listos para construir la llave
     * @throws IllegalArgumentException si el secreto es nulo o vacio
     */
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

    /**
     * Genera un token JWT con correo, id de usuario y roles como claims.
     *
     * @param email correo del usuario autenticado
     * @param usuarioId identificador interno del usuario
     * @param roles lista de roles otorgados (se convierte a claim {@code roles})
     * @return token firmado HS256 con expiracion configurada
     */
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

    /**
     * Valida firma, expiracion y que el subject coincida con el email esperado.
     *
     * @param token JWT recibido
     * @param email correo que debe coincidir con el subject
     * @return {@code true} si es integro, vigente y del usuario indicado
     */
    public boolean validarToken(String token, String email) {
        try {
            Claims claims = extraerClaims(token);
            return email.equals(claims.getSubject()) && !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Indica si la fecha de expiracion del token ya se vencio.
     *
     * @param token JWT recibido
     * @return {@code true} si el token esta expirado
     */
    public boolean isTokenExpired(String token) {
        return extraerExpiration(token).before(new Date());
    }

    /**
     * Extrae y valida todos los claims firmados.
     *
     * @param token JWT recibido
     * @return claims del token
     */
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Obtiene la fecha de expiracion del JWT.
     *
     * @param token JWT recibido
     * @return fecha de expiracion
     */
    public Date extraerExpiration(String token) {
        return extraerClaims(token).getExpiration();
    }

    /**
     * Devuelve el subject (correo) o cadena vacia si no existe.
     *
     * @param token JWT recibido
     * @return email contenido en el subject
     */
    public String extraerEmail(String token) {
        String subject = extraerClaims(token).getSubject();
        return subject == null ? "" : subject;
    }

    /**
     * Lee el claim {@code usuarioId} y lo normaliza a {@link Long}.
     *
     * @param token JWT recibido
     * @return identificador del usuario
     * @throws IllegalArgumentException si el claim existe pero su tipo no es soportado
     */
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

    /**
     * Devuelve la lista de roles del claim {@code roles}. Si no hay roles,
     * retorna una lista vacia.
     *
     * @param token JWT recibido
     * @return lista de roles como strings
     */
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
