package io.github.jaredmcc4.gtm.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JWT Util - Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String SECRET = "test-secret-key-for-jwt-minimum-256-bits-required-for-hs256";
    private final long EXPIRATION = 3600000L; // 1 hora

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Nested
    @DisplayName("generarToken()")
    class GenerarTokenTests {

        @Test
        @DisplayName("Debería generar token válido")
        void deberiaGenerarTokenValido() {
            String email = "test@example.com";
            Long usuarioId = 1L;
            List<String> roles = List.of("USER");

            String token = jwtUtil.generarToken(email, usuarioId, roles);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Debería incluir usuarioId, email y roles en el token")
        void deberiaIncluirDatosEnToken() {
            String email = "test@example.com";
            Long usuarioId = 1L;
            List<String> roles = List.of("USER", "ADMIN");

            String token = jwtUtil.generarToken(email, usuarioId, roles);

            assertThat(jwtUtil.extraerEmail(token)).isEqualTo(email);
            assertThat(jwtUtil.extraerUsuarioId(token)).isEqualTo(usuarioId);
            assertThat(jwtUtil.extraerRoles(token)).containsExactlyInAnyOrder("USER", "ADMIN");
        }

        @Test
        @DisplayName("Debería generar tokens diferentes para datos diferentes")
        void deberiaGenerarTokensDiferentes() {
            String token1 = jwtUtil.generarToken("user1@test.com", 1L, List.of("USER"));
            String token2 = jwtUtil.generarToken("user2@test.com", 2L, List.of("ADMIN"));

            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("validarToken()")
    class ValidarTokenTests {

        @Test
        @DisplayName("Debería validar token correcto")
        void deberiaValidarTokenCorrecto() {
            String email = "test@example.com";
            String token = jwtUtil.generarToken(email, 1L, List.of("USER"));

            boolean esValido = jwtUtil.validarToken(token, email);

            assertThat(esValido).isTrue();
        }

        @Test
        @DisplayName("Debería rechazar token con email incorrecto")
        void deberiaRechazarTokenConEmailIncorrecto() {
            String emailCorrecto = "test@example.com";
            String emailIncorrecto = "otro@example.com";
            String token = jwtUtil.generarToken(emailCorrecto, 1L, List.of("USER"));

            boolean esValido = jwtUtil.validarToken(token, emailIncorrecto);

            assertThat(esValido).isFalse();
        }

        @Test
        @DisplayName("Debería rechazar token malformado")
        void deberiaRechazarTokenMalformado() {
            String tokenMalformado = "token.invalido.malformado";

            assertThatThrownBy(() -> jwtUtil.extraerClaims(tokenMalformado))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Debería rechazar token con firma incorrecta")
        void deberiaRechazarTokenConFirmaIncorrecta() {
            JwtUtil otroJwtUtil = new JwtUtil();
            ReflectionTestUtils.setField(otroJwtUtil, "secret", "otro-secret-key-diferente-para-pruebas-minimo-256-bits");
            ReflectionTestUtils.setField(otroJwtUtil, "expiration", EXPIRATION);

            String email = "test@example.com";
            String tokenOtroSecret = otroJwtUtil.generarToken(email, 1L, List.of("USER"));

            assertThatThrownBy(() -> jwtUtil.validarToken(tokenOtroSecret, email))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Debería rechazar token expirado")
        void deberiaRechazarTokenExpirado() throws InterruptedException {
            JwtUtil jwtUtilExpirado = new JwtUtil();
            ReflectionTestUtils.setField(jwtUtilExpirado, "secret", SECRET);
            ReflectionTestUtils.setField(jwtUtilExpirado, "expiration", 1L); // 1ms de expiración

            String email = "test@example.com";
            String tokenExpirado = jwtUtilExpirado.generarToken(email, 1L, List.of("USER"));

            Thread.sleep(100); // Esperar a que expire

            boolean esValido = jwtUtil.validarToken(tokenExpirado, email);

            assertThat(esValido).isFalse();
        }
    }

    @Nested
    @DisplayName("extraerClaims()")
    class ExtraerClaimsTests {

        @Test
        @DisplayName("Debería extraer usuarioId correctamente")
        void deberiaExtraerUsuarioId() {
            Long usuarioId = 42L;
            String token = jwtUtil.generarToken("test@example.com", usuarioId, List.of("USER"));

            Long resultado = jwtUtil.extraerUsuarioId(token);

            assertThat(resultado).isEqualTo(usuarioId);
        }

        @Test
        @DisplayName("Debería extraer email correctamente")
        void deberiaExtraerEmail() {
            String email = "usuario@example.com";
            String token = jwtUtil.generarToken(email, 1L, List.of("USER"));

            String resultado = jwtUtil.extraerEmail(token);

            assertThat(resultado).isEqualTo(email);
        }

        @Test
        @DisplayName("Debería extraer roles correctamente")
        void deberiaExtraerRoles() {
            List<String> roles = List.of("USER", "ADMIN", "MODERATOR");
            String token = jwtUtil.generarToken("test@example.com", 1L, roles);

            List<String> resultado = jwtUtil.extraerRoles(token);

            assertThat(resultado).containsExactlyInAnyOrder("USER", "ADMIN", "MODERATOR");
        }

        @Test
        @DisplayName("Debería extraer fecha de expiración")
        void deberiaExtraerFechaExpiracion() {
            String token = jwtUtil.generarToken("test@example.com", 1L, List.of("USER"));

            Claims claims = jwtUtil.extraerClaims(token);
            Date expiracion = claims.getExpiration();

            assertThat(expiracion).isAfter(new Date());
            assertThat(expiracion).isBefore(new Date(System.currentTimeMillis() + EXPIRATION + 1000));
        }

        @Test
        @DisplayName("Debería extraer todos los claims")
        void deberiaExtraerTodosClaims() {
            String email = "test@example.com";
            Long usuarioId = 1L;
            List<String> roles = List.of("USER");
            String token = jwtUtil.generarToken(email, usuarioId, roles);

            Claims claims = jwtUtil.extraerClaims(token);

            assertThat(claims.getSubject()).isEqualTo(email);
            assertThat(claims.get("usuarioId", Long.class)).isEqualTo(usuarioId);
            assertThat(claims.get("roles", List.class)).isEqualTo(roles);
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiration()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Verificación de expiración")
    class VerificacionExpiracionTests {

        @Test
        @DisplayName("Token no expirado debería ser válido")
        void tokenNoExpiradoDeberiaSerValido() {
            String email = "test@example.com";
            String token = jwtUtil.generarToken(email, 1L, List.of("USER"));

            boolean esValido = jwtUtil.validarToken(token, email);

            assertThat(esValido).isTrue();
        }

        @Test
        @DisplayName("Token expirado debería ser inválido")
        void tokenExpiradoDeberiaSerInvalido() throws InterruptedException {
            JwtUtil jwtUtilExpirado = new JwtUtil();
            ReflectionTestUtils.setField(jwtUtilExpirado, "secret", SECRET);
            ReflectionTestUtils.setField(jwtUtilExpirado, "expiration", 10L);

            String email = "test@example.com";
            String token = jwtUtilExpirado.generarToken(email, 1L, List.of("USER"));

            Thread.sleep(50);

            boolean esValido = jwtUtil.validarToken(token, email);

            assertThat(esValido).isFalse();
        }

        @Test
        @DisplayName("Debería calcular correctamente tiempo de expiración")
        void deberiaCalcularCorrectamenteTiempoExpiracion() {
            long tiempoInicio = System.currentTimeMillis();
            String token = jwtUtil.generarToken("test@example.com", 1L, List.of("USER"));

            Claims claims = jwtUtil.extraerClaims(token);
            long tiempoExpiracion = claims.getExpiration().getTime();

            long diferencia = tiempoExpiracion - tiempoInicio;

            assertThat(diferencia).isBetween(EXPIRATION - 1000, EXPIRATION + 1000);
        }
    }

    @Nested
    @DisplayName("Casos Edge")
    class CasosEdgeTests {

        @Test
        @DisplayName("Debería manejar email vacío")
        void deberiaManejarEmailVacio() {
            String token = jwtUtil.generarToken("", 1L, List.of("USER"));

            assertThat(token).isNotNull();
            assertThat(jwtUtil.extraerEmail(token)).isEmpty();
        }

        @Test
        @DisplayName("Debería manejar lista de roles vacía")
        void deberiaManejarRolesVacios() {
            String token = jwtUtil.generarToken("test@example.com", 1L, List.of());

            List<String> roles = jwtUtil.extraerRoles(token);

            assertThat(roles).isEmpty();
        }

        @Test
        @DisplayName("Debería manejar usuarioId cero")
        void deberiaManejarUsuarioIdCero() {
            String token = jwtUtil.generarToken("test@example.com", 0L, List.of("USER"));

            Long usuarioId = jwtUtil.extraerUsuarioId(token);

            assertThat(usuarioId).isZero();
        }

        @Test
        @DisplayName("Debería rechazar token null")
        void deberiaRechazarTokenNull() {
            assertThatThrownBy(() -> jwtUtil.extraerEmail(null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Debería rechazar token vacío")
        void deberiaRechazarTokenVacio() {
            assertThatThrownBy(() -> jwtUtil.extraerEmail(""))
                    .isInstanceOf(Exception.class);
        }
    }
}