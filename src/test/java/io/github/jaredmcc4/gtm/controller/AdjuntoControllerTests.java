package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.config.TestSecurityConfig;
import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.dto.adjunto.AdjuntoDto;
import io.github.jaredmcc4.gtm.mapper.AdjuntoMapper;
import io.github.jaredmcc4.gtm.services.AdjuntoService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdjuntoController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Adjunto Controller - Integration Tests")
class AdjuntoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdjuntoService adjuntoService;

    @MockitoBean
    private AdjuntoMapper adjuntoMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);
    }

    @Nested
    @DisplayName("POST /api/v1/adjuntos/tarea/{tareaId}")
    class SubirAdjuntoTests {

        @Test
        @WithMockUser
        @DisplayName("Debería subir archivo correctamente")
        void deberiaSubirArchivo() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "documento.pdf",
                    "application/pdf",
                    "contenido del archivo".getBytes()
            );

            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .nombre("documento.pdf")
                    .path("/uploads/1/documento.pdf")
                    .mimeType("application/pdf")
                    .sizeBytes(file.getSize())
                    .build();

            AdjuntoDto adjuntoDto = AdjuntoDto.builder()
                    .id(1L)
                    .nombre("documento.pdf")
                    .mimeType("application/pdf")
                    .sizeBytes(file.getSize())
                    .build();

            when(adjuntoService.subirAdjunto(eq(1L), any(), eq(1L))).thenReturn(adjunto);
            when(adjuntoMapper.toDto(adjunto)).thenReturn(adjuntoDto);

            mockMvc.perform(multipart("/api/v1/adjuntos/tarea/1")
                            .file(file)
                            .with(csrf())
                            .header("Authorization", "Bearer token"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nombre").value("documento.pdf"));

            verify(adjuntoService).subirAdjunto(eq(1L), any(), eq(1L));
        }

        @Test
        @WithMockUser
        @DisplayName("Debería rechazar archivo vacío")
        void deberiaRechazarArchivoVacio() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "empty.pdf",
                    "application/pdf",
                    new byte[0]
            );

            when(adjuntoService.subirAdjunto(eq(1L), any(), eq(1L)))
                    .thenThrow(new IllegalArgumentException("El archivo está vacío"));

            mockMvc.perform(multipart("/api/v1/adjuntos/tarea/1")
                            .file(file)
                            .with(csrf())
                            .header("Authorization", "Bearer token"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/adjuntos/tarea/{tareaId}")
    class ObtenerAdjuntosTests {

        @Test
        @WithMockUser
        @DisplayName("Debería obtener lista de adjuntos")
        void deberiaObtenerAdjuntos() throws Exception {
            Adjunto adjunto1 = Adjunto.builder()
                    .id(1L)
                    .nombre("archivo1.pdf")
                    .build();

            Adjunto adjunto2 = Adjunto.builder()
                    .id(2L)
                    .nombre("archivo2.pdf")
                    .build();

            AdjuntoDto dto1 = AdjuntoDto.builder()
                    .id(1L)
                    .nombre("archivo1.pdf")
                    .build();

            AdjuntoDto dto2 = AdjuntoDto.builder()
                    .id(2L)
                    .nombre("archivo2.pdf")
                    .build();

            when(adjuntoService.mostrarAdjuntos(1L, 1L))
                    .thenReturn(List.of(adjunto1, adjunto2));
            when(adjuntoMapper.toDto(adjunto1)).thenReturn(dto1);
            when(adjuntoMapper.toDto(adjunto2)).thenReturn(dto2);

            mockMvc.perform(get("/api/v1/adjuntos/tarea/1")
                            .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(adjuntoService).mostrarAdjuntos(1L, 1L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/adjuntos/{id}/descargar")
    class DescargarAdjuntoTests {

        @Test
        @WithMockUser
        @DisplayName("Debería descargar archivo correctamente")
        void deberiaDescargarArchivo() throws Exception {
            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .nombre("documento.pdf")
                    .mimeType("application/pdf")
                    .build();

            Resource resource = new ByteArrayResource("contenido".getBytes());

            when(adjuntoService.obtenerAdjuntoPorId(1L, 1L)).thenReturn(adjunto);
            when(adjuntoService.descargarAdjunto(1L, 1L)).thenReturn(resource);

            mockMvc.perform(get("/api/v1/adjuntos/1/descargar")
                            .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/pdf"))
                    .andExpect(header().exists("Content-Disposition"));

            verify(adjuntoService).descargarAdjunto(1L, 1L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/adjuntos/{id}")
    class EliminarAdjuntoTests {

        @Test
        @WithMockUser
        @DisplayName("Debería eliminar adjunto")
        void deberiaEliminarAdjunto() throws Exception {
            doNothing().when(adjuntoService).eliminarAdjunto(1L, 1L);

            mockMvc.perform(delete("/api/v1/adjuntos/1")
                            .with(csrf())
                            .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Adjunto eliminado exitosamente"));

            verify(adjuntoService).eliminarAdjunto(1L, 1L);
        }
    }
}