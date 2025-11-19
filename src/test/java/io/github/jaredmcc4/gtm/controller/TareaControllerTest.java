package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.response.PageResponse;
import io.github.jaredmcc4.gtm.dto.tarea.ActualizarTareaRequest;
import io.github.jaredmcc4.gtm.dto.tarea.CrearTareaRequest;
import io.github.jaredmcc4.gtm.dto.tarea.TareaDto;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.TareaMapper;
import io.github.jaredmcc4.gtm.services.TareaService;
import io.github.jaredmcc4.gtm.services.UsuarioService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TareaController - Unit Tests")
class TareaControllerTest {

    @Mock
    private TareaService tareaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private TareaMapper tareaMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TareaController tareaController;

    private Jwt jwt;
    private Tarea tarea;
    private TareaDto tareaDto;

    @BeforeEach
    void setUp() {
        jwt = Jwt.withTokenValue("token-value")
                .header("alg", "none")
                .build();
        tarea = Tarea.builder()
                .id(1L)
                .usuario(Usuario.builder().id(1L).email("test@test.com").build())
                .titulo("Demo")
                .build();
        tareaDto = TareaDto.builder().id(1L).titulo("Demo").build();

        when(jwtUtil.extraerUsuarioId("token-value")).thenReturn(1L);
        when(tareaMapper.toDto(any(Tarea.class))).thenReturn(tareaDto);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Page<Tarea> buildPage() {
        return new PageImpl<>(List.of(tarea));
    }

    @Test
    @DisplayName("obtenerTareas debe buscar por texto cuando search está presente")
    void deberiaBuscarTareasPorTexto() {
        when(tareaService.buscarTareasPorTexto(eq(1L), eq("demo"), any(Pageable.class)))
                .thenReturn(buildPage());

        ResponseEntity<ApiResponse<PageResponse<TareaDto>>> response = tareaController.obtenerTareas(
                jwt, 0, 5, "createdAt", "DESC", null, "demo");

        assertThat(response.getBody().getData().getContent()).hasSize(1);
        verify(tareaService).buscarTareasPorTexto(eq(1L), eq("demo"), any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerTareas debe filtrar por estado cuando se proporciona")
    void deberiaFiltrarPorEstado() {
        when(tareaService.filtrarTareas(eq(1L), eq(Tarea.EstadoTarea.PENDIENTE),
                isNull(), isNull(), any(Pageable.class))).thenReturn(buildPage());

        ResponseEntity<ApiResponse<PageResponse<TareaDto>>> response = tareaController.obtenerTareas(
                jwt, 0, 5, "createdAt", "DESC", Tarea.EstadoTarea.PENDIENTE, null);

        assertThat(response.getBody().getData().getContent()).hasSize(1);
        verify(tareaService).filtrarTareas(eq(1L), eq(Tarea.EstadoTarea.PENDIENTE),
                isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerTareas debe usar el flujo por defecto cuando no hay filtros")
    void deberiaObtenerTareasPorDefecto() {
        when(tareaService.obtenerTareasPorUsuarioId(eq(1L), any(Pageable.class)))
                .thenReturn(buildPage());

        ResponseEntity<ApiResponse<PageResponse<TareaDto>>> response = tareaController.obtenerTareas(
                jwt, 0, 5, "createdAt", "DESC", null, null);

        assertThat(response.getBody().getData().getPageSize()).isEqualTo(5);
        verify(tareaService).obtenerTareasPorUsuarioId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Filtrar debe delegar en el servicio y devolver PageResponse")
    void deberiaFiltrarTareas() {
        when(tareaService.filtrarTareas(eq(1L), eq(Tarea.EstadoTarea.CANCELADA),
                eq("docs"), eq(Tarea.Prioridad.ALTA), any(Pageable.class)))
                .thenReturn(buildPage());

        ResponseEntity<ApiResponse<PageResponse<TareaDto>>> response = tareaController.filtrarTareas(
                jwt, Tarea.EstadoTarea.CANCELADA, Tarea.Prioridad.ALTA, "docs", 0, 10);

        assertThat(response.getBody().getData().getContent()).hasSize(1);
    }

    @Test
    @DisplayName("obtenerTareasPorEtiqueta debe usar el servicio correspondiente")
    void deberiaObtenerTareasPorEtiqueta() {
        when(tareaService.obtenerTareasPorEtiquetaId(eq(3L), eq(1L), any(Pageable.class)))
                .thenReturn(buildPage());

        ResponseEntity<ApiResponse<PageResponse<TareaDto>>> response = tareaController.obtenerTareasPorEtiqueta(
                jwt, 3L, 0, 10);

        assertThat(response.getBody().getData().getContent()).hasSize(1);
        verify(tareaService).obtenerTareasPorEtiquetaId(eq(3L), eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerTareasProximasVencer debe mapear la lista del servicio")
    void deberiaObtenerTareasProximas() {
        when(tareaService.obtenerTareasProximasVencimiento(1L, 3))
                .thenReturn(List.of(tarea));

        ResponseEntity<ApiResponse<List<TareaDto>>> response = tareaController.obtenerTareasProximasVencer(jwt, 3);

        assertThat(response.getBody().getData()).containsExactly(tareaDto);
    }

    @Test
    @DisplayName("obtenerEstadisticas debe tomar usuario del SecurityContext cuando no hay JWT")
    void deberiaObtenerEstadisticasDesdeContexto() {
        Jwt contextoJwt = Jwt.withTokenValue("context-token")
                .header("alg", "none")
                .build();
        when(jwtUtil.extraerUsuarioId("context-token")).thenReturn(9L);

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(contextoJwt);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        when(tareaService.contarTareasPorEstado(9L, Tarea.EstadoTarea.PENDIENTE)).thenReturn(2L);
        when(tareaService.contarTareasPorEstado(9L, Tarea.EstadoTarea.COMPLETADA)).thenReturn(1L);
        when(tareaService.contarTareasPorEstado(9L, Tarea.EstadoTarea.CANCELADA)).thenReturn(1L);

        ResponseEntity<ApiResponse<Object>> response = tareaController.obtenerEstadisticas(null);

        @SuppressWarnings("unchecked")
        Map<String, Long> body = (Map<String, Long>) response.getBody().getData();
        assertThat(body.get("total")).isEqualTo(4L);
    }

    @Test
    @DisplayName("obtenerTareaPorId debe delegar en el servicio")
    void deberiaObtenerTareaPorId() {
        when(tareaService.obtenerTareaPorIdYUsuarioId(7L, 1L)).thenReturn(tarea);

        ResponseEntity<ApiResponse<TareaDto>> response = tareaController.obtenerTareaPorId(jwt, 7L);

        assertThat(response.getBody().getData().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("crearTarea debe construir la entidad y delegar en el servicio")
    void deberiaCrearTarea() {
        CrearTareaRequest request = CrearTareaRequest.builder()
                .titulo("Nueva")
                .descripcion("Desc")
                .prioridad(Tarea.Prioridad.ALTA)
                .build();

        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(Usuario.builder().id(1L).email("test@test.com").build());
        when(tareaService.crearTarea(any(Tarea.class), any(Usuario.class))).thenReturn(tarea);

        ResponseEntity<ApiResponse<TareaDto>> response = tareaController.crearTarea(jwt, request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        verify(tareaService).crearTarea(any(Tarea.class), any(Usuario.class));
    }

    @Test
    @DisplayName("actualizarTarea debe parsear la fecha de vencimiento cuando existe")
    void deberiaActualizarTarea() {
        ActualizarTareaRequest request = ActualizarTareaRequest.builder()
                .titulo("Actualizada")
                .descripcion("Desc")
                .prioridad(Tarea.Prioridad.BAJA)
                .estado(Tarea.EstadoTarea.COMPLETADA)
                .fechaVencimiento(LocalDateTime.now().toString())
                .build();

        when(tareaService.actualizarTarea(eq(1L), any(Tarea.class), eq(1L)))
                .thenReturn(tarea);

        ResponseEntity<ApiResponse<TareaDto>> response = tareaController.actualizarTarea(jwt, 1L, request);

        assertThat(response.getBody().getData().getId()).isEqualTo(1L);
        verify(tareaService).actualizarTarea(eq(1L), any(Tarea.class), eq(1L));
    }

    @Test
    @DisplayName("eliminarTarea debe invocar al servicio")
    void deberiaEliminarTarea() {
        ResponseEntity<ApiResponse<Void>> response = tareaController.eliminarTarea(jwt, 8L);

        assertThat(response.getBody().getMessage()).contains("eliminada");
        verify(tareaService).eliminarTarea(8L, 1L);
    }

    @Test
    @DisplayName("obtenerTareas debe lanzar Unauthorized si no hay token")
    void deberiaLanzarCuandoNoHayToken() {
        assertThatThrownBy(() -> tareaController.obtenerTareas(null, 0, 5, "createdAt", "DESC", null, null))
                .isInstanceOf(UnauthorizedException.class);
    }
}
