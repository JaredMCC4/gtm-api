package io.github.jaredmcc4.gtm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.usuario.CambiarPasswordRequest;
import io.github.jaredmcc4.gtm.dto.usuario.UsuarioDto;
import io.github.jaredmcc4.gtm.mapper.UsuarioMapper;
import io.github.jaredmcc4.gtm.services.UsuarioService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import io.github.jaredmcc4.gtm.config.TestSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Usuario Controller - Integration Tests")
class UsuarioControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioMapper usuarioMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = UsuarioTestBuilder.unUsuario().conId(1L).build();
        when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);
    }

    @Nested
    @DisplayName("GET /api/v1/usuarios/perfil")
    class ObtenerPerfilTests {

        @Test
        @WithMockUser
        @DisplayName("Debería obtener perfil del usuario autenticado")
        void deberiaObtenerPerfil() throws Exception {
            UsuarioDto usuarioDto = UsuarioDto.builder()
                    .id(1L)
                    .email(usuario.getEmail())
                    .nombreUsuario(usuario.getNombreUsuario())
                    .build();

            when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(usuario);
            when(usuarioMapper.toDto(usuario)).thenReturn(usuarioDto);

            mockMvc.perform(get("/api/v1/usuarios/perfil")
                            .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value(usuario.getEmail()));

            verify(usuarioService).obtenerUsuarioPorId(1L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/usuarios/cambiar-password")
    class CambiarPasswordTests {

        @Test
        @WithMockUser
        @DisplayName("Debería cambiar contraseña correctamente")
        void deberiaCambiarPassword() throws Exception {
            CambiarPasswordRequest request = new CambiarPasswordRequest(
                    "OldPassword123!",
                    "NewPassword123!"
            );

            doNothing().when(usuarioService).cambiarPassword(1L, "OldPassword123!", "NewPassword123!");

            mockMvc.perform(patch("/api/v1/usuarios/cambiar-password")
                            .with(csrf())
                            .header("Authorization", "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Contrasena cambiada exitosamente"));

            verify(usuarioService).cambiarPassword(1L, "OldPassword123!", "NewPassword123!");
        }

        @Test
        @WithMockUser
        @DisplayName("Debería validar contraseña actual")
        void deberiaValidarPasswordActual() throws Exception {
            CambiarPasswordRequest request = new CambiarPasswordRequest(
                    "",
                    "NewPassword123!"
            );

            mockMvc.perform(patch("/api/v1/usuarios/cambiar-password")
                            .with(csrf())
                            .header("Authorization", "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(usuarioService, never()).cambiarPassword(anyLong(), anyString(), anyString());
        }
    }
}