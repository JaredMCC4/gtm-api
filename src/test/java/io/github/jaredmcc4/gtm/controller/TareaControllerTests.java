package io.github.jaredmcc4.gtm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jaredmcc4.gtm.builders.TareaTestBuilder;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.config.TestSecurityConfig;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.tarea.CrearTareaRequest;
import io.github.jaredmcc4.gtm.dto.tarea.ActualizarTareaRequest;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TareaController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Tarea Controller - Integration Tests")
class TareaControllerTests {

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    );
            return http.build();
        }
    }

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

    @BeforeEach
    void setUp() {
        usuario = UsuarioTestBuilder.unUsuario().conId(1L).build();
        tarea = TareaTestBuilder.unaTarea()
                .conId(1L)
                .conUsuario(usuario)
                .conTitulo("Tarea de prueba")
                .build();

        when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);
        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(usuario);
    }

    @Nested
    @DisplayName("POST /api/tareas")
    class CrearTareaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería poder crear una tarea correctamente")
        void deberiaCrearTarea() throws Exception {
            // Arrange
            CrearTareaRequest request = CrearTareaRequest.builder()
                    .titulo("Nueva tarea")
                    .descripcion("Descripción de la tarea")
                    .prioridad(Tarea.Prioridad.MEDIA)
                    .fechaVencimiento(LocalDateTime.now().plusDays(7))
                    .build();

            when(tareaService.crearTarea(any(Tarea.class), any(Usuario.class))).thenReturn(tarea);

            mockMvc.perform(post("/api/tareas")
                            .with(csrf())
                            .header("Authorization", "Bearer jwt.token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Tarea creada exitosamente"));

            verify(tareaService).crearTarea(any(Tarea.class), eq(usuario));
        }

        @Test
        @WithMockUser
        @DisplayName("Debería retornar 400 con título vacío")
        void deberiaRechazarTituloVacio() throws Exception {

            CrearTareaRequest request = CrearTareaRequest.builder()
                    .titulo("")
                    .build();

            mockMvc.perform(post("/api/tareas")
                            .with(csrf())
                            .header("Authorization", "Bearer jwt.token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.titulo").exists());

            verify(tareaService, never()).crearTarea(any(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Debería retornar 400 con título menor a 3 carácteres")
        void deberiaRechazarTituloCorto() throws Exception {

            CrearTareaRequest request = CrearTareaRequest.builder()
                    .titulo("ab")
                    .build();

            mockMvc.perform(post("/api/tareas")
                            .with(csrf())
                            .header("Authorization", "Bearer jwt.token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debería retornar 401 sin token de autorización")
        void deberiaRechazarSinToken() throws Exception {

            CrearTareaRequest request = CrearTareaRequest.builder()
                    .titulo("Nueva tarea")
                    .build();

            mockMvc.perform(post("/api/tareas")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/tareas")
    class ObtenerTareasTests {

        @Test
        @WithMockUser
        @DisplayName("Debería poder obtener las tareas paginadas")
        void deberiaObtenerTareasPaginadas() throws Exception {

            Page<Tarea> page = new PageImpl<>(
                    List.of(tarea),
                    PageRequest.of(0, 10),
                    1
            );

            when(tareaService.obtenerTareasPorUsuarioId(eq(1L), any())).thenReturn(page);

            mockMvc.perform(get("/api/tareas")
                            .header("Authorization", "Bearer jwt.token")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));

            verify(tareaService).obtenerTareasPorUsuarioId(eq(1L), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Debería filtrar las tareas por estado")
        void deberiaFiltrarPorEstado() throws Exception {

            Page<Tarea> page = new PageImpl<>(List.of(tarea), PageRequest.of(0, 10), 1);
            when(tareaService.filtrarTareas(eq(1L), any(), any(), any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/tareas")
                            .header("Authorization", "Bearer jwt.token")
                            .param("estado", "PENDIENTE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());

            verify(tareaService).filtrarTareas(
                    eq(1L),
                    eq(Tarea.EstadoTarea.PENDIENTE),
                    isNull(),
                    isNull(),
                    any()
            );
        }

        @Test
        @WithMockUser
        @DisplayName("Debería poder buscar tareas por texto")
        void deberiaBuscarPorTexto() throws Exception {

            Page<Tarea> page = new PageImpl<>(List.of(tarea), PageRequest.of(0, 10), 1);
            when(tareaService.buscarTareasPorTexto(eq(1L), anyString(), any())).thenReturn(page);

            mockMvc.perform(get("/api/tareas")
                            .header("Authorization", "Bearer jwt.token")
                            .param("search", "prueba"))
                    .andExpect(status().isOk());

            verify(tareaService).buscarTareasPorTexto(eq(1L), eq("prueba"), any());
        }
    }

    @Nested
    @DisplayName("GET /api/tareas/{id}")
    class ObtenerTareaPorIdTests {

        @Test
        @WithMockUser
        @DisplayName("Debería poder obtener una tarea por su ID")
        void deberiaObtenerTareaPorId() throws Exception {

            when(tareaService.obtenerTareaPorIdYUsuarioId(1L, 1L)).thenReturn(tarea);

            mockMvc.perform(get("/api/tareas/1")
                            .header("Authorization", "Bearer jwt.token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());

            verify(tareaService).obtenerTareaPorIdYUsuarioId(1L, 1L);
        }

        @Test
        @WithMockUser
        @DisplayName("Debería retornar 404 cuando una tarea no existe")
        void deberiaRetornar404CuandoNoExiste() throws Exception {

            when(tareaService.obtenerTareaPorIdYUsuarioId(999L, 1L))
                    .thenThrow(new ResourceNotFoundException("Tarea no encontrada"));

            mockMvc.perform(get("/api/tareas/999")
                            .header("Authorization", "Bearer jwt.token"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/tareas/{id}")
    class ActualizarTareaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería actualizar una tarea correctamente")
        void deberiaActualizarTarea() throws Exception {

            ActualizarTareaRequest request = ActualizarTareaRequest.builder()
                    .titulo("Título actualizado")
                    .prioridad(Tarea.Prioridad.ALTA)
                    .build();

            when(tareaService.actualizarTarea(eq(1L), any(Tarea.class), eq(1L)))
                    .thenReturn(tarea);

            mockMvc.perform(put("/api/tareas/1")
                            .with(csrf())
                            .header("Authorization", "Bearer jwt.token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Tarea actualizada exitosamente"));

            verify(tareaService).actualizarTarea(eq(1L), any(Tarea.class), eq(1L));
        }

        @Test
        @WithMockUser
        @DisplayName("Debería retornar 404 al actualizar una tarea inexistente")
        void deberiaRetornar404AlActualizarInexistente() throws Exception {

            ActualizarTareaRequest request = ActualizarTareaRequest.builder()
                    .titulo("Título actualizado")
                    .build();

            when(tareaService.actualizarTarea(eq(999L), any(Tarea.class), eq(1L)))
                    .thenThrow(new ResourceNotFoundException("Tarea no encontrada"));

            mockMvc.perform(put("/api/tareas/999")
                            .with(csrf())
                            .header("Authorization", "Bearer jwt.token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/tareas/{id}")
    class EliminarTareaTests {

        @Test
        @WithMockUser
        @DisplayName("Debería eliminar una tarea correctamente")
        void deberiaEliminarTarea() throws Exception {

            doNothing().when(tareaService).eliminarTarea(1L, 1L);

            mockMvc.perform(delete("/api/tareas/1")
                            .with(csrf())
                            .header("Authorization", "Bearer jwt.token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Tarea eliminada exitosamente"));

            verify(tareaService).eliminarTarea(1L, 1L);
        }

        @Test
        @WithMockUser
        @DisplayName("Debería retornar 404 al eliminar una tarea inexistente")
        void deberiaRetornar404AlEliminarInexistente() throws Exception {

            doThrow(new ResourceNotFoundException("Tarea no encontrada"))
                    .when(tareaService).eliminarTarea(999L, 1L);


            mockMvc.perform(delete("/api/tareas/999")
                            .with(csrf())
                            .header("Authorization", "Bearer jwt.token"))
                    .andExpect(status().isNotFound());
        }
    }
}