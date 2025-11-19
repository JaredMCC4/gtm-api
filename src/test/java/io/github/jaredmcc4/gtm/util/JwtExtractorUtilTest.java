package io.github.jaredmcc4.gtm.util;

import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtExtractorUtil - Unit Tests")
class JwtExtractorUtilTest {

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
    @DisplayName("Debe extraer usuarioId de diferentes tipos de claim")
    void deberiaExtraerUsuarioId() {
        assertThat(JwtExtractorUtil.extractUsuarioId(buildJwt(5L, "user@test.com"))).isEqualTo(5L);
        assertThat(JwtExtractorUtil.extractUsuarioId(buildJwt(7, "user@test.com"))).isEqualTo(7L);
        assertThat(JwtExtractorUtil.extractUsuarioId(buildJwt("9", "user@test.com"))).isEqualTo(9L);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el claim usuarioId no existe")
    void deberiaFallarSinUsuarioId() {
        assertThatThrownBy(() -> JwtExtractorUtil.extractUsuarioId(buildJwt(null, "user@test.com")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el token es null")
    void deberiaFallarConTokenNull() {
        assertThatThrownBy(() -> JwtExtractorUtil.extractUsuarioId(null))
                .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> JwtExtractorUtil.extractEmail(null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Debe extraer el email del claim sub")
    void deberiaExtraerEmail() {
        assertThat(JwtExtractorUtil.extractEmail(buildJwt(1L, "user@test.com")))
                .isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el email está vacío")
    void deberiaFallarEmailVacio() {
        Jwt jwt = buildJwt(1L, "");
        assertThatThrownBy(() -> JwtExtractorUtil.extractEmail(jwt))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuarioId tiene formato inválido")
    void deberiaFallarPorFormatoInvalido() {
        Jwt jwt = buildJwt(new Object(), "user@test.com");
        assertThatThrownBy(() -> JwtExtractorUtil.extractUsuarioId(jwt))
                .isInstanceOf(UnauthorizedException.class);
    }
}
