package io.github.jaredmcc4.gtm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.config.TestSecurityConfig;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.auth.JwtResponse;
import io.github.jaredmcc4.gtm.dto.auth.LoginRequest;
import io.github.jaredmcc4.gtm.dto.auth.RefreshTokenRequest;
import io.github.jaredmcc4.gtm.dto.auth.RegistroRequest;
import io.github.jaredmcc4.gtm.services.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Auth Controller - Integration Tests")
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Nested
    @DisplayName("POST /api/auth/registro")
    class RegistroTests {

        @Test
        @DisplayName("Debería poder registrar un usuario con datos válidos")
        void deberiaRegistrarUsuario() throws Exception {

            RegistroRequest request = new RegistroRequest(
                    "nuevo@example.com",
                    "Password123!",
                    "Nuevo Usuario",
                    "America/Costa_Rica"
            );
            Usuario usuario = UsuarioTestBuilder.unUsuario()
                    .conEmail(request.getEmail())
                    .conNombreUsuario(request.getNombreUsuario())
                    .build();

            when(authService.registrarUsuario(any(RegistroRequest.class))).thenReturn(usuario);

            mockMvc.perform(post("/api/auth/registro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("nuevo@example.com"))
                    .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"));

            verify(authService).registrarUsuario(any(RegistroRequest.class));
        }

        @Test
        @DisplayName("Debería retornar 400 con email inválido")
        void deberiaRechazarEmailInvalido() throws Exception {

            RegistroRequest request = new RegistroRequest(
                    "email-invalido",
                    "Password123!",
                    "Usuario",
                    "America/Costa_Rica"
            );

            mockMvc.perform(post("/api/auth/registro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors.email").exists());

            verify(authService, never()).registrarUsuario(any());
        }

        @Test
        @DisplayName("Debería retornar 400 con contraseña muy corta")
        void deberiaRechazarPasswordCorta() throws Exception {

            RegistroRequest request = new RegistroRequest(
                    "test@example.com",
                    "Pass1!",
                    "Usuario",
                    "America/Costa_Rica"
            );

            mockMvc.perform(post("/api/auth/registro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").exists());

            verify(authService, never()).registrarUsuario(any());
        }

        @Test
        @DisplayName("Debería retornar 400 cuando email está en blanco")
        void deberiaRechazarEmailEnBlanco() throws Exception {

            RegistroRequest request = new RegistroRequest(
                    "",
                    "Password123!",
                    "Usuario",
                    "America/Costa_Rica"
            );

            mockMvc.perform(post("/api/auth/registro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists());
        }

        @Test
        @DisplayName("Debería retornar 409 cuando email ya existe")
        void deberiaRechazarEmailDuplicado() throws Exception {

            RegistroRequest request = new RegistroRequest(
                    "existente@example.com",
                    "Password123!",
                    "Usuario",
                    "America/Costa_Rica"
            );

            when(authService.registrarUsuario(any(RegistroRequest.class)))
                    .thenThrow(new IllegalArgumentException("Ya existe un usuario con el email proporcionado"));

            mockMvc.perform(post("/api/auth/registro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Debería autenticar usuario con credenciales válidas")
        void deberiaAutenticarUsuario() throws Exception {

            LoginRequest request = new LoginRequest("test@example.com", "Password123!");
            JwtResponse jwtResponse = JwtResponse.builder()
                    .jwtToken("jwt.token.here")
                    .type("Bearer")
                    .expiresIn(3600000L)
                    .refreshToken("refresh-token-uuid")
                    .build();

            when(authService.autenticarUsuario(any(LoginRequest.class))).thenReturn(jwtResponse);
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.jwtToken").value("jwt.token.here"))
                    .andExpect(jsonPath("$.data.type").value("Bearer"))
                    .andExpect(jsonPath("$.data.refreshToken").exists())
                    .andExpect(jsonPath("$.data.expiresIn").value(3600000));

            verify(authService).autenticarUsuario(any(LoginRequest.class));
        }

        @Test
        @DisplayName("Debería retornar 401 con credenciales incorrectas")
        void deberiaRechazarCredencialesIncorrectas() throws Exception {

            LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

            when(authService.autenticarUsuario(any(LoginRequest.class)))
                    .thenThrow(new IllegalArgumentException("Credenciales inválidas"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
        }

        @Test
        @DisplayName("Debería retornar 400 con datos de entrada inválidos")
        void deberiaValidarDatosEntrada() throws Exception {

            LoginRequest request = new LoginRequest("", "");

            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists())
                    .andExpect(jsonPath("$.errors.password").exists());

            verify(authService, never()).autenticarUsuario(any());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshTokenTests {

        @Test
        @DisplayName("Debería refrescar el token correctamente")
        void deberiaRefrescarToken() throws Exception {

            RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
            JwtResponse jwtResponse = JwtResponse.builder()
                    .jwtToken("new.jwt.token")
                    .type("Bearer")
                    .expiresIn(3600000L)
                    .refreshToken("valid-refresh-token")
                    .build();

            when(authService.refrescarToken(anyString())).thenReturn(jwtResponse);

            mockMvc.perform(post("/api/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.jwtToken").value("new.jwt.token"));

            verify(authService).refrescarToken("valid-refresh-token");
        }

        @Test
        @DisplayName("Debería retornar 401 con refresh token inválido")
        void deberiaRechazarRefreshTokenInvalido() throws Exception {

            RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

            when(authService.refrescarToken(anyString()))
                    .thenThrow(new IllegalArgumentException("Refresh token inválido o expirado"));

            mockMvc.perform(post("/api/auth/refresh")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @WithMockUser
        @DisplayName("Debería poder cerrar sesión correctamente")
        void deberiaCerrarSesion() throws Exception {

            RefreshTokenRequest request = new RefreshTokenRequest("token-to-revoke");
            doNothing().when(authService).cerrarSesion(anyString());

            mockMvc.perform(post("/api/auth/logout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Sesión cerrada exitosamente"));

            verify(authService).cerrarSesion("token-to-revoke");
        }
    }
}