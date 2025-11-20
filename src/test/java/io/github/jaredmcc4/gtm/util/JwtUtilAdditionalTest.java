package io.github.jaredmcc4.gtm.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUtil - Cobertura adicional")
class JwtUtilAdditionalTest {

    private JwtUtil jwtUtil;
    private final String SECRET = "test-secret-key-for-jwt-minimum-256-bits-required-for-hs256";
    private final long EXPIRATION = 3600000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    private String buildToken(Object usuarioIdClaim, Object rolesClaim) {
        Key key = (Key) ReflectionTestUtils.invokeMethod(jwtUtil, "getSigningKey");
        var builder = Jwts.builder()
                .setSubject("helper@test.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION));
        if (usuarioIdClaim != null) {
            builder.claim("usuarioId", usuarioIdClaim);
        }
        if (rolesClaim != null) {
            builder.claim("roles", rolesClaim);
        }
        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Nested
    @DisplayName("decodeSecret()")
    class DecodeSecretTests {

        @Test
        @DisplayName("Debe decodificar secretos en Base64 estándar")
        void deberiaDecodificarBase64() {
            String base64Plain = "base64-secret-key-para-tests-largos-y-seguro-para-256bits";
            String base64 = Base64.getEncoder()
                    .encodeToString(base64Plain.getBytes(StandardCharsets.UTF_8));
            ReflectionTestUtils.setField(jwtUtil, "secret", base64);

            String token = jwtUtil.generarToken("base64@test.com", 1L, List.of("USER"));

            assertThat(jwtUtil.validarToken(token, "base64@test.com")).isTrue();
        }

        @Test
        @DisplayName("Debe decodificar secretos en Base64 URL safe")
        void deberiaDecodificarBase64Url() {
            String base64UrlPlain = "url-safe-secret-key-para-pruebas-y-muy-largo-para-256bits";
            String base64Url = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(base64UrlPlain.getBytes(StandardCharsets.UTF_8));
            ReflectionTestUtils.setField(jwtUtil, "secret", base64Url);

            String token = jwtUtil.generarToken("url@test.com", 2L, List.of("ADMIN"));

            assertThat(jwtUtil.extraerEmail(token)).isEqualTo("url@test.com");
        }

        @Test
        @DisplayName("Debe usar texto plano cuando el secreto no es Base64")
        void deberiaUsarTextoPlano() {
            String plainSecret = "texto-plano-seguro-para-tests-12345678901234567890";
            ReflectionTestUtils.setField(jwtUtil, "secret", plainSecret);

            String token = jwtUtil.generarToken("plain@test.com", 3L, List.of("USER"));

            assertThat(jwtUtil.extraerUsuarioId(token)).isEqualTo(3L);
        }

        @Test
        @DisplayName("Debe rechazar secretos vacíos o nulos")
        void deberiaRechazarSecretosVacios() {
            assertThatThrownBy(() ->
                    ReflectionTestUtils.invokeMethod(jwtUtil, "decodeSecret", "")
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("extraerUsuarioId() casos adicionales")
    class ExtraerUsuarioIdAdicionalTests {

        @Test
        @DisplayName("Debe convertir claims numéricos genéricos")
        void deberiaConvertirNumberGenerico() {
            String token = buildToken(12.0, List.of("USER"));

            assertThat(jwtUtil.extraerUsuarioId(token)).isEqualTo(12L);
        }

        @Test
        @DisplayName("Debe fallar cuando el claim no es numérico")
        void deberiaFallarConStringNoNumerico() {
            String token = buildToken("abc", List.of("USER"));

            assertThatThrownBy(() -> jwtUtil.extraerUsuarioId(token))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("extraerRoles() casos edge")
    class ExtraerRolesEdgeTests {

        @Test
        @DisplayName("Debe devolver lista vacía cuando los roles no son lista")
        void deberiaRetornarListaVacia() {
            String token = buildToken(1L, "ADMIN");

            assertThat(jwtUtil.extraerRoles(token)).isEmpty();
        }

        @Test
        @DisplayName("Debe limpiar nulos dentro de la lista de roles")
        void deberiaLimpiarRolesNulos() {
            String token = buildToken(1L, Arrays.asList("USER", null, "ADMIN"));

            assertThat(jwtUtil.extraerRoles(token))
                    .containsExactlyInAnyOrder("USER", "ADMIN");
        }
    }

    @Test
    @DisplayName("validarToken debe retornar false para tokens inválidos")
    void validarTokenDebeRetornarFalse() {
        assertThat(jwtUtil.validarToken("token.invalido", "test@example.com")).isFalse();
    }
}
