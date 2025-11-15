package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.domain.RefreshToken;
import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.auth.JwtResponse;
import io.github.jaredmcc4.gtm.dto.auth.LoginRequest;
import io.github.jaredmcc4.gtm.dto.auth.RegistroRequest;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.RefreshTokenRepository;
import io.github.jaredmcc4.gtm.repository.RolRepository;
import io.github.jaredmcc4.gtm.repository.UsuarioRepository;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service - Unit Tests")
public class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private Rol rolUser;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpiration", 3600000L);
        rolUser = Rol.builder()
                .id(1L)
                .nombreRol("USER")
                .build();

        usuario = UsuarioTestBuilder.unUsuario()
                .conId(1L)
                .conEmail("test@test.com")
                .conContrasenaHash("$2b$12$RoIfpG6/4G3lPAGqaUr2Iu39pWqwBi0g/WxYOVmaH.Ab9mnsketb.") // contrasena459.
                .conRol(rolUser)
                .build();
    }

    @Nested
    @DisplayName("registrarUsuario()")
    class RegistrarUsuarioTests {

        @Test
        @DisplayName("Debería poder registrar un usuario correctamente")
        void deberiaRegistrarUsuario() {
            RegistroRequest request = new RegistroRequest(
                    "nuevo@test.com",
                    "password123",
                    "Nuevo Usuario",
                    "America/Costa_Rica"
            );

            when(usuarioRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(rolRepository.findByNombreRol("USER")).thenReturn(Optional.of(rolUser));
            when(passwordEncoder.encode(request.getPassword())).thenReturn("$2a$12$encoded");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

            Usuario resultado = authService.registrarUsuario(request);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getEmail()).isEqualTo("nuevo@test.com");
            assertThat(resultado.getNombreUsuario()).isEqualTo("Nuevo Usuario");
            assertThat(resultado.isActivo()).isTrue();
            assertThat(resultado.getRoles()).contains(rolUser);

            verify(usuarioRepository).existsByEmail(request.getEmail());
            verify(passwordEncoder).encode(request.getPassword());
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Debería rechazar un email duplicado")
        void deberiaRechazarEmailDuplicado() {
            RegistroRequest request = new RegistroRequest(
                    "test@test.com",
                    "password123",
                    "Usuario",
                    "America/Costa_Rica"
            );

            when(usuarioRepository.existsByEmail(request.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> authService.registrarUsuario(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Ya existe un usuario con el email");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería lanzar una excepción cuando el rol USER no exista")
        void deberiaLanzarExcepcionSinRolUser() {
            RegistroRequest request = new RegistroRequest(
                    "nuevo@prueba.com",
                    "password123",
                    "Usuario",
                    "America/Costa_Rica"
            );

            when(usuarioRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(rolRepository.findByNombreRol("USER")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.registrarUsuario(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Rol de usuario no existe");
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("autenticarUsuario()")
    class AutenticarUsuarioTests {

        @Test
        @DisplayName("Debería autenticar al usuario correctamente")
        void deberiaAutenticarUsuario() {

            LoginRequest request = new LoginRequest("test@test.com", "contrasena459.");
            String jwtToken = "jwt.token.here";
            String refreshTokenValue = "refresh-token-uuid";

            when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(request.getPassword(), usuario.getContrasenaHash())).thenReturn(true);
            when(jwtUtil.generarToken(anyString(), anyLong(), anyList())).thenReturn(jwtToken);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(inv -> {
                        RefreshToken rt = inv.getArgument(0);
                        ReflectionTestUtils.setField(rt, "token", refreshTokenValue);
                        return rt;
                    });

            JwtResponse response = authService.autenticarUsuario(request);

            assertThat(response).isNotNull();
            assertThat(response.getJwtToken()).isEqualTo(jwtToken);
            assertThat(response.getType()).isEqualTo("Bearer");
            assertThat(response.getRefreshToken()).isNotNull();
            assertThat(response.getExpiresIn()).isEqualTo(3600000L);

            verify(passwordEncoder).matches(request.getPassword(), usuario.getContrasenaHash());
            verify(jwtUtil).generarToken(usuario.getEmail(), usuario.getId(), List.of("USER"));
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Debería rechazar credenciales incorrectas")
        void deberiaRechazarCredencialesIncorrectas() {
            LoginRequest request = new LoginRequest("test@test.com", "wrongPassword");

            when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(request.getPassword(), usuario.getContrasenaHash())).thenReturn(false);

            assertThatThrownBy(() -> authService.autenticarUsuario(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Credenciales inválidas");

            verify(jwtUtil, never()).generarToken(anyString(), anyLong(), anyList());
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar al usuario inactivo")
        void deberiaRechazarUsuarioInactivo() {
            // Arrange
            LoginRequest request = new LoginRequest("test@test.com", "contrasena459.");
            Usuario usuarioInactivo = UsuarioTestBuilder.unUsuario()
                    .conEmail("test@test.com")
                    .inactivo()
                    .build();

            when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(usuarioInactivo));
            when(passwordEncoder.matches(request.getPassword(), usuarioInactivo.getContrasenaHash())).thenReturn(true);

            assertThatThrownBy(() -> authService.autenticarUsuario(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Inactivo");

            verify(jwtUtil, never()).generarToken(anyString(), anyLong(), anyList());
        }

        @Test
        @DisplayName("Debería rechazar un email no registrado")
        void deberiaRechazarEmailNoRegistrado() {

            LoginRequest request = new LoginRequest("noexiste@example.com", "password123");

            when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.autenticarUsuario(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Credenciales inválidas");
        }
    }

    @Nested
    @DisplayName("refrescarToken()")
    class RefrescarTokenTests {

        @Test
        @DisplayName("Debería refrescar el token correctamente")
        void deberiaRefrescarToken() {
            // Arrange
            String refreshTokenValue = "valid-refresh-token";
            String newJwtToken = "new.jwt.token";

            RefreshToken refreshToken = RefreshToken.builder()
                    .id(1L)
                    .usuario(usuario)
                    .token(refreshTokenValue)
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));
            when(jwtUtil.generarToken(anyString(), anyLong(), anyList())).thenReturn(newJwtToken);

            JwtResponse response = authService.refrescarToken(refreshTokenValue);

            assertThat(response).isNotNull();
            assertThat(response.getJwtToken()).isEqualTo(newJwtToken);
            assertThat(response.getRefreshToken()).isEqualTo(refreshTokenValue);

            verify(jwtUtil).generarToken(usuario.getEmail(), usuario.getId(), List.of("USER"));
        }

        @Test
        @DisplayName("Debería rechazar un refresh token revocado")
        void deberiaRechazarTokenRevocado() {

            String refreshTokenValue = "revoked-token";
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenValue)
                    .revoked(true)
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .build();

            when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));

            assertThatThrownBy(() -> authService.refrescarToken(refreshTokenValue))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(jwtUtil, never()).generarToken(anyString(), anyLong(), anyList());
        }

        @Test
        @DisplayName("Debería rechazar un refresh token expirado")
        void deberiaRechazarTokenExpirado() {
            String refreshTokenValue = "expired-token";
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenValue)
                    .revoked(false)
                    .expiresAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));

            assertThatThrownBy(() -> authService.refrescarToken(refreshTokenValue))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Debería rechazar un refresh token inexistente")
        void deberiaRechazarTokenInexistente() {
            when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refrescarToken("invalid-token"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("cerrarSesion()")
    class CerrarSesionTests {

        @Test
        @DisplayName("Debería revocar el refresh token correctamente")
        void deberiaRevocarToken() {

            String refreshTokenValue = "token-to-revoke";
            RefreshToken refreshToken = RefreshToken.builder()
                    .id(1L)
                    .token(refreshTokenValue)
                    .revoked(false)
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .build();

            when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(refreshToken));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

            authService.cerrarSesion(refreshTokenValue);

            verify(refreshTokenRepository).save(argThat(token ->
                    token.getRevoked() == true
            ));
        }

        @Test
        @DisplayName("Debería lanzar una excepción si el token no existe")
        void deberiaLanzarExcepcionSiTokenNoExiste() {
            when(refreshTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.cerrarSesion("nonexistent"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("validarToken()")
    class ValidarTokenTests {

        @Test
        @DisplayName("Debería poder validar el token correctamente")
        void deberiaValidarToken() {
            String token = "valid.jwt.token";
            String email = "test@test.com";

            when(jwtUtil.extraerEmail(token)).thenReturn(email);
            when(jwtUtil.validarToken(token, email)).thenReturn(true);

            assertThatCode(() -> authService.validarToken(token))
                    .doesNotThrowAnyException();

            verify(jwtUtil).extraerEmail(token);
            verify(jwtUtil).validarToken(token, email);
        }

        @Test
        @DisplayName("Debería rechazar un token inválido")
        void deberiaRechazarTokenInvalido() {
            String token = "invalid.jwt.token";
            String email = "test@test.com";

            when(jwtUtil.extraerEmail(token)).thenReturn(email);
            when(jwtUtil.validarToken(token, email)).thenReturn(false);

            assertThatThrownBy(() -> authService.validarToken(token))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}