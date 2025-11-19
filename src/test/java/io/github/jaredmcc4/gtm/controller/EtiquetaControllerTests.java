package io.github.jaredmcc4.gtm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.config.TestSecurityConfig;
import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
import io.github.jaredmcc4.gtm.mapper.EtiquetaMapper;
import io.github.jaredmcc4.gtm.services.EtiquetaService;
import io.github.jaredmcc4.gtm.services.UsuarioService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EtiquetaController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Etiqueta Controller - Integration Tests")
class EtiquetaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EtiquetaService etiquetaService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private EtiquetaMapper etiquetaMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = UsuarioTestBuilder.unUsuario().conId(1L).build();
        when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);
        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    }

    @Nested
    @DisplayName("POST /api/v1/etiquetas")
    class CrearEtiquetaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería crear etiqueta correctamente")
        void deberiaCrearEtiqueta() throws Exception {
            EtiquetaDto requestDto = EtiquetaDto.builder()
                    .nombre("Trabajo")
                    .colorHex("#FF5733")
                    .build();

            Etiqueta etiqueta = Etiqueta.builder()
                    .nombre("Trabajo")
                    .colorHex("#FF5733")
                    .build();

            Etiqueta etiquetaCreada = Etiqueta.builder()
                    .id(1L)
                    .nombre("Trabajo")
                    .colorHex("#FF5733")
                    .usuario(usuario)
                    .build();

            EtiquetaDto responseDto = EtiquetaDto.builder()
                    .id(1L)
                    .nombre("Trabajo")
                    .colorHex("#FF5733")
                    .build();

            when(etiquetaMapper.toEntity(any(EtiquetaDto.class))).thenReturn(etiqueta);
            when(etiquetaService.crearEtiqueta(any(Etiqueta.class), eq(usuario))).thenReturn(etiquetaCreada);
            when(etiquetaMapper.toDto(etiquetaCreada)).thenReturn(responseDto);

            mockMvc.perform(post("/api/v1/etiquetas")
                            .with(csrf())
                            .header("Authorization", "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nombre").value("Trabajo"))
                    .andExpect(jsonPath("$.data.colorHex").value("#FF5733"));

            verify(etiquetaService).crearEtiqueta(any(Etiqueta.class), eq(usuario));
        }

        @Test
        @WithMockUser
        @DisplayName("Debería validar nombre obligatorio")
        void deberiaValidarNombre() throws Exception {
            EtiquetaDto requestDto = EtiquetaDto.builder()
                    .colorHex("#FF5733")
                    .build();

            mockMvc.perform(post("/api/v1/etiquetas")
                            .with(csrf())
                            .header("Authorization", "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.nombre").exists());

            verify(etiquetaService, never()).crearEtiqueta(any(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Debería validar formato de color hexadecimal")
        void deberiaValidarFormatoColor() throws Exception {
            EtiquetaDto requestDto = EtiquetaDto.builder()
                    .nombre("Trabajo")
                    .colorHex("rojo")
                    .build();

            mockMvc.perform(post("/api/v1/etiquetas")
                            .with(csrf())
                            .header("Authorization", "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.colorHex").exists());

            verify(etiquetaService, never()).crearEtiqueta(any(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/etiquetas")
    class ObtenerEtiquetasTests {

        @Test
        @WithMockUser
        @DisplayName("Debería obtener lista de etiquetas")
        void deberiaObtenerEtiquetas() throws Exception {
            Etiqueta etiqueta1 = Etiqueta.builder()
                    .id(1L)
                    .nombre("Trabajo")
                    .colorHex("#FF5733")
                    .usuario(usuario)
                    .build();

            Etiqueta etiqueta2 = Etiqueta.builder()
                    .id(2L)
                    .nombre("Personal")
                    .colorHex("#33FF57")
                    .usuario(usuario)
                    .build();

            EtiquetaDto dto1 = EtiquetaDto.builder()
                    .id(1L)
                    .nombre("Trabajo")
                    .colorHex("#FF5733")
                    .build();

            EtiquetaDto dto2 = EtiquetaDto.builder()
                    .id(2L)
                    .nombre("Personal")
                    .colorHex("#33FF57")
                    .build();

            when(etiquetaService.obtenerEtiquetasPorUsuarioId(1L))
                    .thenReturn(List.of(etiqueta1, etiqueta2));
            when(etiquetaMapper.toDto(etiqueta1)).thenReturn(dto1);
            when(etiquetaMapper.toDto(etiqueta2)).thenReturn(dto2);

            mockMvc.perform(get("/api/v1/etiquetas")
                            .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(etiquetaService).obtenerEtiquetasPorUsuarioId(1L);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/etiquetas/{id}")
    class ActualizarEtiquetaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería actualizar etiqueta")
        void deberiaActualizarEtiqueta() throws Exception {
            EtiquetaDto requestDto = EtiquetaDto.builder()
                    .nombre("Trabajo Urgente")
                    .colorHex("#FF0000")
                    .build();

            Etiqueta etiqueta = Etiqueta.builder()
                    .nombre("Trabajo Urgente")
                    .colorHex("#FF0000")
                    .build();

            Etiqueta etiquetaActualizada = Etiqueta.builder()
                    .id(1L)
                    .nombre("Trabajo Urgente")
                    .colorHex("#FF0000")
                    .usuario(usuario)
                    .build();

            EtiquetaDto responseDto = EtiquetaDto.builder()
                    .id(1L)
                    .nombre("Trabajo Urgente")
                    .colorHex("#FF0000")
                    .build();

            when(etiquetaMapper.toEntity(any(EtiquetaDto.class))).thenReturn(etiqueta);
            when(etiquetaService.actualizarEtiqueta(eq(1L), any(Etiqueta.class), eq(1L)))
                    .thenReturn(etiquetaActualizada);
            when(etiquetaMapper.toDto(etiquetaActualizada)).thenReturn(responseDto);

            mockMvc.perform(put("/api/v1/etiquetas/1")
                            .with(csrf())
                            .header("Authorization", "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nombre").value("Trabajo Urgente"));

            verify(etiquetaService).actualizarEtiqueta(eq(1L), any(Etiqueta.class), eq(1L));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/etiquetas/{id}")
    class EliminarEtiquetaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería eliminar etiqueta")
        void deberiaEliminarEtiqueta() throws Exception {
            doNothing().when(etiquetaService).eliminarEtiqueta(1L, 1L);

            mockMvc.perform(delete("/api/v1/etiquetas/1")
                            .with(csrf())
                            .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Etiqueta eliminada exitosamente"));

            verify(etiquetaService).eliminarEtiqueta(1L, 1L);
        }
    }
}