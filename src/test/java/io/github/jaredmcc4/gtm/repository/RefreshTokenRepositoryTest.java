package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.RefreshToken;
import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application.properties")
@DisplayName("RefreshTokenRepository - Integration Tests")
class RefreshTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        Rol rolUser = rolRepository.findByNombreRol("USER")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombreRol("USER").build()));

        usuario = Usuario.builder()
                .email("test@example.com")
                .contrasenaHash("$2a$12$hash")
                .nombreUsuario("Usuario Test")
                .activo(true)
                .roles(Set.of(rolUser))
                .build();
        usuario = usuarioRepository.save(usuario);
        entityManager.flush();
    }

    @Nested
    @DisplayName("findByToken()")
    class FindByTokenTests {

        @Test
        @DisplayName("Debería encontrar token por string")
        void deberiaEncontrarToken() {
            String tokenString = UUID.randomUUID().toString();
            crearToken(tokenString, false, LocalDateTime.now().plusDays(1));
            entityManager.flush();

            Optional<RefreshToken> resultado = refreshTokenRepository.findByToken(tokenString);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getToken()).isEqualTo(tokenString);
        }

        @Test
        @DisplayName("Debería retornar vacío si token no existe")
        void deberiaRetornarVacioSiNoExiste() {
            Optional<RefreshToken> resultado = refreshTokenRepository.findByToken("token-inexistente");

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByTokenAndRevokedFalse()")
    class FindByTokenAndRevokedFalseTests {

        @Test
        @DisplayName("Debería encontrar token no revocado")
        void deberiaEncontrarTokenNoRevocado() {
            String tokenString = UUID.randomUUID().toString();
            crearToken(tokenString, false, LocalDateTime.now().plusDays(1));
            entityManager.flush();

            Optional<RefreshToken> resultado = refreshTokenRepository.findByTokenAndRevokedFalse(tokenString);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getToken()).isEqualTo(tokenString);
            assertThat(resultado.get().getRevoked()).isFalse();
        }

        @Test
        @DisplayName("No debería encontrar tokens revocados")
        void noDeberiaEncontrarTokensRevocados() {
            String tokenString = UUID.randomUUID().toString();
            crearToken(tokenString, true, LocalDateTime.now().plusDays(1));
            entityManager.flush();

            Optional<RefreshToken> resultado = refreshTokenRepository.findByTokenAndRevokedFalse(tokenString);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("No debería encontrar token inexistente")
        void noDeberiaEncontrarTokenInexistente() {
            Optional<RefreshToken> resultado = refreshTokenRepository.findByTokenAndRevokedFalse("inexistente");

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteExpiredTokens()")
    class DeleteExpiredTokensTests {

        @Test
        @DisplayName("Debería eliminar tokens expirados")
        void deberiaEliminarTokensExpirados() {
            crearToken(UUID.randomUUID().toString(), false, LocalDateTime.now().minusDays(1));
            crearToken(UUID.randomUUID().toString(), false, LocalDateTime.now().minusHours(1));
            crearToken(UUID.randomUUID().toString(), false, LocalDateTime.now().plusDays(1));
            entityManager.flush();

            refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            entityManager.flush();

            long count = refreshTokenRepository.count();
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("No debería eliminar tokens válidos")
        void noDeberiaEliminarTokensValidos() {
            crearToken(UUID.randomUUID().toString(), false, LocalDateTime.now().plusDays(1));
            crearToken(UUID.randomUUID().toString(), false, LocalDateTime.now().plusHours(1));
            entityManager.flush();

            refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            entityManager.flush();

            long count = refreshTokenRepository.count();
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("deleteByUsuarioId()")
    class DeleteByUsuarioIdTests {

        @Test
        @DisplayName("Debería eliminar todos los tokens de un usuario")
        void deberiaEliminarTokensDelUsuario() {
            crearToken(UUID.randomUUID().toString(), false, LocalDateTime.now().plusDays(1));
            crearToken(UUID.randomUUID().toString(), false, LocalDateTime.now().plusDays(1));
            entityManager.flush();

            refreshTokenRepository.deleteByUsuarioId(usuario.getId());
            entityManager.flush();

            long count = refreshTokenRepository.count();
            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Operaciones CRUD")
    class OperacionesCrudTests {

        @Test
        @DisplayName("Debería guardar y recuperar token")
        void deberiaGuardarYRecuperarToken() {
            String tokenString = UUID.randomUUID().toString();
            RefreshToken token = crearToken(tokenString, false, LocalDateTime.now().plusDays(1));
            entityManager.flush();
            entityManager.clear();

            Optional<RefreshToken> recuperado = refreshTokenRepository.findById(token.getId());

            assertThat(recuperado).isPresent();
            assertThat(recuperado.get().getToken()).isEqualTo(tokenString);
            assertThat(recuperado.get().getUsuario().getId()).isEqualTo(usuario.getId());
        }

        @Test
        @DisplayName("Debería actualizar estado de revocación")
        void deberiaActualizarEstadoRevocacion() {
            String tokenString = UUID.randomUUID().toString();
            RefreshToken token = crearToken(tokenString, false, LocalDateTime.now().plusDays(1));
            entityManager.flush();

            token.setRevoked(true);
            refreshTokenRepository.save(token);
            entityManager.flush();
            entityManager.clear();

            Optional<RefreshToken> actualizado = refreshTokenRepository.findById(token.getId());

            assertThat(actualizado).isPresent();
            assertThat(actualizado.get().getRevoked()).isTrue();
        }
    }

    private RefreshToken crearToken(String token, boolean revoked, LocalDateTime expiresAt) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .usuario(usuario)
                .expiresAt(expiresAt)
                .revoked(revoked)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}