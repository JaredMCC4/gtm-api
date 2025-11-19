package io.github.jaredmcc4.gtm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jaredmcc4.gtm.config.TestSecurityConfig;
import io.github.jaredmcc4.gtm.domain.Subtarea;
import io.github.jaredmcc4.gtm.dto.subtarea.SubtareaDto;
import io.github.jaredmcc4.gtm.mapper.SubtareaMapper;
import io.github.jaredmcc4.gtm.services.SubtareaService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
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

@WebMvcTest(SubtareaController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Subtarea Controller - Integration Tests")
class SubtareaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubtareaService subtareaService;

    @MockitoBean
    private SubtareaMapper subtareaMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("GET /api/v1/subtareas/tarea/{tareaId}")
    class ObtenerSubtareasTests {

        @Test
        @WithMockUser
        @DisplayName("Debería obtener subtareas de la tarea")
        void deberiaObtenerSubtareas() throws Exception {
            Subtarea subtarea1 = Subtarea.builder()
                    .id(1L)
                    .titulo("Subtarea 1")
                    .completada(false)
                    .build();
            Subtarea subtarea2 = Subtarea.builder()
                    .id(2L)
                    .titulo("Subtarea 2")
                    .completada(true)
                    .build();

            SubtareaDto dto1 = SubtareaDto.builder()
                    .id(1L)
                    .titulo("Subtarea 1")
                    .completada(false)
                    .build();
            SubtareaDto dto2 = SubtareaDto.builder()
                    .id(2L)
                    .titulo("Subtarea 2")
                    .completada(true)
                    .build();

            when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);
            when(subtareaService.mostrarSubtareas(1L, 1L))
                    .thenReturn(List.of(subtarea1, subtarea2));
            when(subtareaMapper.toDto(subtarea1)).thenReturn(dto1);
            when(subtareaMapper.toDto(subtarea2)).thenReturn(dto2);

            mockMvc.perform(get("/api/v1/subtareas/tarea/1")
                            .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].titulo").value("Subtarea 1"))
                    .andExpect(jsonPath("$.data[1].completada").value(true));

            verify(subtareaService).mostrarSubtareas(1L, 1L);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/subtareas/tarea/{tareaId}")
    class CrearSubtareaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería crear subtarea correctamente")
        void deberiaCrearSubtarea() throws Exception {
            SubtareaDto requestDto = SubtareaDto.builder()
                    .titulo("Nueva subtarea")
                    .completada(false)
                    .build();

            Subtarea subtarea = Subtarea.builder()
                    .titulo("Nueva subtarea")
                    .completada(false)
                    .build();

            Subtarea subtareaCreada = Subtarea.builder()
                    .id(1L)
                    .titulo("Nueva subtarea")
                    .completada(false)
                    .build();

            SubtareaDto responseDto = SubtareaDto.builder()
                    .id(1L)
                    .titulo("Nueva subtarea")
                    .completada(false)
                    .build();

            when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);
            when(subtareaMapper.toEntity(any(SubtareaDto.class))).thenReturn(subtarea);
            when(subtareaService.crearSubtarea(eq(1L), any(Subtarea.class), eq(1L)))
                    .thenReturn(subtareaCreada);
            when(subtareaMapper.toDto(subtareaCreada)).thenReturn(responseDto);

            mockMvc.perform(post("/api/v1/subtareas/tarea/1")
                            .with(csrf())
                            .header("Authorization", "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.titulo").value("Nueva subtarea"));

            verify(subtareaService).crearSubtarea(eq(1L), any(Subtarea.class), eq(1L));
        }

        @Test
        @WithMockUser
        @DisplayName("Debería rechazar título vacío")
        void deberiaRechazarTituloVacio() throws Exception {
            SubtareaDto requestDto = SubtareaDto.builder()
                    .titulo("")
                    .build();

            when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);

            mockMvc.perform(post("/api/v1/subtareas/tarea/1")
                            .with(csrf())
                            .header("Authorization", "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());

            verify(subtareaService, never()).crearSubtarea(anyLong(), any(), anyLong());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/subtareas/{id}")
    class ActualizarSubtareaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería actualizar subtarea correctamente")
        void deberiaActualizarSubtarea() throws Exception {
            SubtareaDto requestDto = SubtareaDto.builder()
                    .titulo("Título actualizado")
                    .completada(true)
                    .build();

            Subtarea subtarea = Subtarea.builder()
                    .titulo("Título actualizado")
                    .completada(true)
                    .build();

            Subtarea subtareaActualizada = Subtarea.builder()
                    .id(1L)
                    .titulo("Título actualizado")
                    .completada(true)
                    .build();

            SubtareaDto responseDto = SubtareaDto.builder()
                    .id(1L)
                    .titulo("Título actualizado")
                    .completada(true)
                    .build();

            when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);
            when(subtareaMapper.toEntity(any(SubtareaDto.class))).thenReturn(subtarea);
            when(subtareaService.actualizarSubtarea(eq(1L), any(Subtarea.class), eq(1L)))
                    .thenReturn(subtareaActualizada);
            when(subtareaMapper.toDto(subtareaActualizada)).thenReturn(responseDto);

            mockMvc.perform(put("/api/v1/subtareas/1")
                            .with(csrf())
                            .header("Authorization", "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.titulo").value("Título actualizado"))
                    .andExpect(jsonPath("$.data.completada").value(true));

            verify(subtareaService).actualizarSubtarea(eq(1L), any(Subtarea.class), eq(1L));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/subtareas/{id}")
    class EliminarSubtareaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería eliminar subtarea correctamente")
        void deberiaEliminarSubtarea() throws Exception {
            when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);
            doNothing().when(subtareaService).eliminarSubtarea(1L, 1L);

            mockMvc.perform(delete("/api/v1/subtareas/1")
                            .with(csrf())
                            .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Subtarea eliminada exitosamente"));

            verify(subtareaService).eliminarSubtarea(1L, 1L);
        }
    }
}