package io.github.jaredmcc4.gtm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jaredmcc4.gtm.builders.TareaTestBuilder;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.config.TestSecurityConfig;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.tarea.ActualizarTareaRequest;
import io.github.jaredmcc4.gtm.dto.tarea.CrearTareaRequest;
import io.github.jaredmcc4.gtm.dto.tarea.TareaDto;
import io.github.jaredmcc4.gtm.exception.GlobalExceptionHandler;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.mapper.TareaMapper;
import io.github.jaredmcc4.gtm.services.TareaService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TareaController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc
@DisplayName("Tarea Controller - Integration Tests")
class TareaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TareaService tareaService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private TareaMapper tareaMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Usuario usuario;
    private Tarea tarea;
    private TareaDto tareaDto;

    @BeforeEach
    void setUp() {
        usuario = UsuarioTestBuilder.unUsuario().conId(1L).build();
        tarea = TareaTestBuilder.unaTarea()
                .conId(1L)
                .conUsuario(usuario)
                .conTitulo("Tarea de prueba")
                .build();

        tareaDto = TareaDto.builder()
                .id(1L)
                .titulo("Tarea de prueba")
                .build();

        when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);
        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(usuario);
        when(tareaMapper.toDto(any(Tarea.class))).thenReturn(tareaDto);
    }

    private Jwt jwtMock() {
        return Jwt.withTokenValue("token-mock")
                .header("alg", "none")
                .claim("sub", "user-id")
                .build();
    }

    private Page<Tarea> buildPage() {
        return new PageImpl<>(List.of(tarea), PageRequest.of(0, 10), 1);
    }

    @Nested
    @DisplayName("POST /api/v1/tareas")
    class CrearTareaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería poder crear una tarea correctamente")
        void deberiaCrearTarea() throws Exception {
            CrearTareaRequest request = CrearTareaRequest.builder()
                    .titulo("Nueva tarea")
                    .descripcion("Descripción")
                    .prioridad(Tarea.Prioridad.MEDIA)
                    .fechaVencimiento(LocalDateTime.now().plusDays(7))
                    .build();

            when(tareaService.crearTarea(any(Tarea.class), any(Usuario.class))).thenReturn(tarea);

            mockMvc.perform(post("/api/v1/tareas")
                            .with(csrf())
                            .with(jwt().jwt(jwtMock()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));

            verify(tareaService).crearTarea(any(Tarea.class), eq(usuario));
        }

        @Test
        @WithMockUser
        @DisplayName("Debería retornar 400 con título vacío")
        void deberiaRechazarTituloVacio() throws Exception {
            CrearTareaRequest request = CrearTareaRequest.builder().titulo("").build();

            mockMvc.perform(post("/api/v1/tareas")
                            .with(csrf())
                            .with(jwt().jwt(jwtMock()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(tareaService, never()).crearTarea(any(), any());
        }

        @Test
        @DisplayName("Debería retornar 401 sin token de autorización")
        void deberiaRechazarSinToken() throws Exception {
            CrearTareaRequest request = CrearTareaRequest.builder().titulo("Nueva tarea").build();

            mockMvc.perform(post("/api/v1/tareas")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tareas")
    class ObtenerTareasTests {

        @Test
        @WithMockUser
        @DisplayName("Debería obtener tareas paginadas por defecto")
        void deberiaObtenerTareasPaginadas() throws Exception {
            when(tareaService.obtenerTareasPorUsuarioId(eq(1L), any())).thenReturn(buildPage());

            mockMvc.perform(get("/api/v1/tareas")
                            .with(jwt().jwt(jwtMock()))
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());

            verify(tareaService).obtenerTareasPorUsuarioId(eq(1L), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Debería filtrar las tareas por estado")
        void deberiaFiltrarPorEstado() throws Exception {
            when(tareaService.filtrarTareas(eq(1L), eq(Tarea.EstadoTarea.PENDIENTE), isNull(), isNull(), any()))
                    .thenReturn(buildPage());

            mockMvc.perform(get("/api/v1/tareas")
                            .with(jwt().jwt(jwtMock()))
                            .param("estado", "PENDIENTE"))
                    .andExpect(status().isOk());

            verify(tareaService).filtrarTareas(eq(1L), eq(Tarea.EstadoTarea.PENDIENTE), isNull(), isNull(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Debería buscar tareas por texto")
        void deberiaBuscarPorTexto() throws Exception {
            when(tareaService.buscarTareasPorTexto(eq(1L), eq("prueba"), any())).thenReturn(buildPage());

            mockMvc.perform(get("/api/v1/tareas")
                            .with(jwt().jwt(jwtMock()))
                            .param("search", "prueba"))
                    .andExpect(status().isOk());

            verify(tareaService).buscarTareasPorTexto(eq(1L), eq("prueba"), any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tareas/filtrar")
    class FiltrarTareasTests {
        @Test
        @WithMockUser
        @DisplayName("Debería filtrar tareas con múltiples parámetros")
        void deberiaFiltrarTareas() throws Exception {
            when(tareaService.filtrarTareas(eq(1L), eq(Tarea.EstadoTarea.CANCELADA), eq("docs"), eq(Tarea.Prioridad.ALTA), any()))
                    .thenReturn(buildPage());

            mockMvc.perform(get("/api/v1/tareas/filtrar")
                            .with(jwt().jwt(jwtMock()))
                            .param("estado", "CANCELADA")
                            .param("prioridad", "ALTA")
                            .param("titulo", "docs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tareas/etiqueta/{etiquetaId}")
    class ObtenerPorEtiquetaTests {
        @Test
        @WithMockUser
        @DisplayName("Debería obtener tareas por etiqueta")
        void deberiaObtenerTareasPorEtiqueta() throws Exception {
            when(tareaService.obtenerTareasPorEtiquetaId(eq(3L), eq(1L), any(Pageable.class)))
                    .thenReturn(buildPage());

            mockMvc.perform(get("/api/v1/tareas/etiqueta/3")
                            .with(jwt().jwt(jwtMock())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());

            verify(tareaService).obtenerTareasPorEtiquetaId(eq(3L), eq(1L), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tareas/proximas-vencer")
    class ProximasVencerTests {
        @Test
        @WithMockUser
        @DisplayName("Debería obtener tareas próximas a vencer")
        void deberiaObtenerTareasProximas() throws Exception {
            when(tareaService.obtenerTareasProximasVencimiento(1L, 3))
                    .thenReturn(List.of(tarea));

            mockMvc.perform(get("/api/v1/tareas/proximas-vencer")
                            .with(jwt().jwt(jwtMock()))
                            .param("dias", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tareas/estadisticas")
    class EstadisticasTests {
        @Test
        @WithMockUser
        @DisplayName("Debería obtener estadísticas")
        void deberiaObtenerEstadisticas() throws Exception {
            when(tareaService.contarTareasPorEstado(1L, Tarea.EstadoTarea.PENDIENTE)).thenReturn(2L);
            when(tareaService.contarTareasPorEstado(1L, Tarea.EstadoTarea.COMPLETADA)).thenReturn(1L);
            when(tareaService.contarTareasPorEstado(1L, Tarea.EstadoTarea.CANCELADA)).thenReturn(1L);

            mockMvc.perform(get("/api/v1/tareas/estadisticas")
                            .with(jwt().jwt(jwtMock())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(4))
                    .andExpect(jsonPath("$.data.pendientes").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tareas/{id}")
    class ObtenerTareaPorIdTests {

        @Test
        @WithMockUser
        @DisplayName("Debería poder obtener una tarea por su ID")
        void deberiaObtenerTareaPorId() throws Exception {
            when(tareaService.obtenerTareaPorIdYUsuarioId(1L, 1L)).thenReturn(tarea);

            mockMvc.perform(get("/api/v1/tareas/1")
                            .with(jwt().jwt(jwtMock())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser
        @DisplayName("Debería retornar 404 cuando una tarea no existe")
        void deberiaRetornar404CuandoNoExiste() throws Exception {
            when(tareaService.obtenerTareaPorIdYUsuarioId(999L, 1L))
                    .thenThrow(new ResourceNotFoundException("Tarea no encontrada"));

            mockMvc.perform(get("/api/v1/tareas/999")
                            .with(jwt().jwt(jwtMock())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/tareas/{id}")
    class ActualizarTareaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería actualizar una tarea correctamente")
        void deberiaActualizarTarea() throws Exception {
            ActualizarTareaRequest request = ActualizarTareaRequest.builder()
                    .titulo("Título actualizado")
                    .prioridad(Tarea.Prioridad.ALTA)
                    .build();

            when(tareaService.actualizarTarea(eq(1L), any(Tarea.class), eq(1L))).thenReturn(tarea);

            mockMvc.perform(put("/api/v1/tareas/1")
                            .with(csrf())
                            .with(jwt().jwt(jwtMock()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("Debería retornar 404 al actualizar una tarea inexistente")
        void deberiaRetornar404AlActualizarInexistente() throws Exception {
            ActualizarTareaRequest request = ActualizarTareaRequest.builder().titulo("Título actualizado").build();

            when(tareaService.actualizarTarea(eq(999L), any(Tarea.class), eq(1L)))
                    .thenThrow(new ResourceNotFoundException("Tarea no encontrada"));

            mockMvc.perform(put("/api/v1/tareas/999")
                            .with(csrf())
                            .with(jwt().jwt(jwtMock()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/tareas/{id}")
    class EliminarTareaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería eliminar una tarea correctamente")
        void deberiaEliminarTarea() throws Exception {
            doNothing().when(tareaService).eliminarTarea(1L, 1L);

            mockMvc.perform(delete("/api/v1/tareas/1")
                            .with(csrf())
                            .with(jwt().jwt(jwtMock())))
                    .andExpect(status().isOk());

            verify(tareaService).eliminarTarea(1L, 1L);
        }

        @Test
        @WithMockUser
        @DisplayName("Debería retornar 404 al eliminar una tarea inexistente")
        void deberiaRetornar404AlEliminarInexistente() throws Exception {
            doThrow(new ResourceNotFoundException("Tarea no encontrada"))
                    .when(tareaService).eliminarTarea(999L, 1L);

            mockMvc.perform(delete("/api/v1/tareas/999")
                            .with(csrf())
                            .with(jwt().jwt(jwtMock())))
                    .andExpect(status().isNotFound());
        }
    }
}