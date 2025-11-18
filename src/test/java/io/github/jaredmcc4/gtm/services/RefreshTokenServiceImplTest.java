package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.domain.RefreshToken;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshToken Service - Unit Tests")
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = UsuarioTestBuilder.unUsuario().conId(1L).build();
    }

    @Nested
    @DisplayName("crearRefreshToken()")
    class CrearRefreshTokenTests {

        @Test
        @DisplayName("Debería crear refresh token correctamente")
        void deberiaCrearRefreshToken() {
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            RefreshToken resultado = refreshTokenService.crearRefreshToken(usuario, 1440); // 24h

            assertThat(resultado).isNotNull();
            assertThat(resultado.getToken()).isNotNull();
            assertThat(resultado.getToken()).hasSize(36); // UUID length
            assertThat(resultado.getUsuario()).isEqualTo(usuario);
            assertThat(resultado.getExpiresAt()).isAfter(LocalDateTime.now());
            assertThat(resultado.getRevoked()).isFalse();

            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("validarRefreshToken()")
    class ValidarRefreshTokenTests {

        @Test
        @DisplayName("Debería validar token no expirado")
        void deberiaValidarTokenValido() {
            RefreshToken token = RefreshToken.builder()
                    .token("valid-token")
                    .usuario(usuario)
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-token"))
                    .thenReturn(Optional.of(token));

            Optional<RefreshToken> resultado = refreshTokenService.validarRefreshToken("valid-token");

            assertThat(resultado).isPresent();
            assertThat(resultado.get()).isEqualTo(token);
        }

        @Test
        @DisplayName("Debería rechazar token expirado")
        void deberiaRechazarTokenExpirado() {
            RefreshToken token = RefreshToken.builder()
                    .token("expired-token")
                    .usuario(usuario)
                    .expiresAt(LocalDateTime.now().minusDays(1))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findByTokenAndRevokedFalse("expired-token"))
                    .thenReturn(Optional.of(token));

            Optional<RefreshToken> resultado = refreshTokenService.validarRefreshToken("expired-token");

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Debería rechazar token revocado")
        void deberiaRechazarTokenRevocado() {
            when(refreshTokenRepository.findByTokenAndRevokedFalse("revoked-token"))
                    .thenReturn(Optional.empty());

            Optional<RefreshToken> resultado = refreshTokenService.validarRefreshToken("revoked-token");

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("revocarRefreshToken()")
    class RevocarTokenTests {

        @Test
        @DisplayName("Debería revocar token correctamente")
        void deberiaRevocarToken() {
            RefreshToken token = RefreshToken.builder()
                    .token("token-to-revoke")
                    .usuario(usuario)
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findByToken("token-to-revoke"))
                    .thenReturn(Optional.of(token));
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            refreshTokenService.revocarRefreshToken("token-to-revoke");

            assertThat(token.getRevoked()).isTrue();
            verify(refreshTokenRepository).save(token);
        }

        @Test
        @DisplayName("No debería fallar con token inexistente")
        void noDeberiaFallarConTokenInexistente() {
            when(refreshTokenRepository.findByToken("nonexistent"))
                    .thenReturn(Optional.empty());

            assertThatCode(() -> refreshTokenService.revocarRefreshToken("nonexistent"))
                    .doesNotThrowAnyException();

            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("limpiarRefreshTokensExpirados()")
    class LimpiarTokensExpiradosTests {

        @Test
        @DisplayName("Debería eliminar tokens expirados")
        void deberiaEliminarTokensExpirados() {
            doNothing().when(refreshTokenRepository)
                    .deleteExpiredTokens(any(LocalDateTime.class));

            assertThatCode(() -> refreshTokenService.limpiarRefreshTokensExpirados())
                    .doesNotThrowAnyException();

            verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
        }
    }
}