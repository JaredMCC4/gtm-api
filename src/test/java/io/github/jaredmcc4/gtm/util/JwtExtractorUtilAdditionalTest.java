package io.github.jaredmcc4.gtm.util;

import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtExtractorUtil - Cobertura adicional")
class JwtExtractorUtilAdditionalTest {

    private Jwt buildJwt(Object usuarioId, String email) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none");
        if (usuarioId != null) {
            builder.claim("usuarioId", usuarioId);
        }
        if (email != null) {
            builder.claim("sub", email);
        }
        return builder.build();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuarioId no es numérico")
    void deberiaLanzarExcepcionPorStringNoNumerico() {
        Jwt jwt = buildJwt("abc", "user@test.com");

        assertThatThrownBy(() -> JwtExtractorUtil.extractUsuarioId(jwt))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el email no está presente")
    void deberiaLanzarExcepcionSinEmail() {
        Jwt jwt = buildJwt(1L, null);

        assertThatThrownBy(() -> JwtExtractorUtil.extractEmail(jwt))
                .isInstanceOf(UnauthorizedException.class);
    }
}
