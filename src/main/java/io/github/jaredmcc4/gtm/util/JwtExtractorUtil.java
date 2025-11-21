package io.github.jaredmcc4.gtm.util;

import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Utilidades para extraer datos basicos de un JWT ya validado por el resource server.
 * Se usa en controladores y servicios para recuperar el contexto de usuario autenticado.
 */
@Component
public class JwtExtractorUtil {

    /**
     * Extrae el ID de usuario desde el claim {@code usuarioId} de un JWT validado.
     * Convierte valores numericos o cadenas numericas a {@link Long} y valida su presencia.
     *
     * @param jwt token JWT ya validado por el resource server
     * @return identificador interno del usuario autenticado
     * @throws UnauthorizedException si el token es nulo, no contiene el claim o el formato no es numerico
     */
    public static Long extractUsuarioId(Jwt jwt) {
        if (jwt == null) {
            throw new UnauthorizedException("Token JWT no proporcionado");
        }

        try {
            Object usuarioIdClaim = jwt.getClaim("usuarioId");

            if (usuarioIdClaim == null) {
                throw new UnauthorizedException("El token no contiene el ID de usuario");
            }

            if (usuarioIdClaim instanceof Long) {
                return (Long) usuarioIdClaim;
            } else if (usuarioIdClaim instanceof Integer) {
                return ((Integer) usuarioIdClaim).longValue();
            } else if (usuarioIdClaim instanceof String) {
                return Long.valueOf((String) usuarioIdClaim);
            } else if (usuarioIdClaim instanceof Number) {
                return ((Number) usuarioIdClaim).longValue();
            } else {
                throw new UnauthorizedException("Formato de ID de usuario invalido en el token");
            }
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("El ID de usuario en el token no es valido");
        }
    }

    /**
     * Obtiene el correo electronico desde el subject del JWT.
     *
     * @param jwt token JWT ya validado por el resource server
     * @return email del usuario autenticado
     * @throws UnauthorizedException si el token es nulo o no incluye subject
     */
    public static String extractEmail(Jwt jwt) {
        if (jwt == null) {
            throw new UnauthorizedException("Token JWT no proporcionado");
        }

        String email = jwt.getClaimAsString("sub");
        if (email == null || email.isEmpty()) {
            throw new UnauthorizedException("El token no contiene el email del usuario");
        }

        return email;
    }
}

