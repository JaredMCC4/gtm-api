package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Tarea;
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
import io.github.jaredmcc4.gtm.util.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/tareas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tareas", description = "Gestión de todas las tareas del usuario.")
public class TareaController {

    private final TareaService tareaService;
    private final UsuarioService usuarioService;
    private final TareaMapper tareaMapper;
    private final JwtUtil jwtUtil;

    private Long resolverUsuarioId(Jwt jwt) {
        if (jwt != null) {
            return jwtUtil.extraerUsuarioId(jwt.getTokenValue());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtUtil.extraerUsuarioId(jwtAuth.getToken().getTokenValue());
        }
        throw new UnauthorizedException("Token JWT requerido.");
    }

    @Operation(summary = "Obtener todas las tareas del usuario", description = "Muestra una lista de tareas ordenadas por fecha de creación descendente.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TareaDto>>> obtenerTareas(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) Tarea.EstadoTarea estado,
            @RequestParam(required = false) String search
    ) {
        Long usuarioId = resolverUsuarioId(jwt);

        log.info("GET /api/v1/tareas - Usuario ID: {}, Page: {}, Size: {}", usuarioId, page, size);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Tarea> tareaPage;
        if (search != null) {
            tareaPage = tareaService.buscarTareasPorTexto(usuarioId, search, pageable);
        } else if (estado != null) {
            tareaPage = tareaService.filtrarTareas(usuarioId, estado, null, null, pageable);
        } else {
            tareaPage = tareaService.obtenerTareasPorUsuarioId(usuarioId, pageable);
        }

        PageResponse<TareaDto> pageResponse = PageUtil.toPageResponse(tareaPage, tareaMapper::toDto);

        return ResponseEntity.ok(ApiResponse.success("Tareas obtenidas exitosamente", pageResponse));
    }

    @Operation(summary = "Buscar tareas por texto", description = "Búsqueda en título y descripción.")
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<PageResponse<TareaDto>>> buscarTareas(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Texto a buscar") @RequestParam String texto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/buscar - Usuario ID: {}, Texto: '{}'", usuarioId, texto);

        Pageable pageable = PageRequest.of(page, size);
        Page<Tarea> tareaPage = tareaService.buscarTareasPorTexto(usuarioId, texto, pageable);
        PageResponse<TareaDto> pageResponse = PageUtil.toPageResponse(tareaPage, tareaMapper::toDto);

        return ResponseEntity.ok(ApiResponse.success("Búsqueda completada", pageResponse));
    }

    @Operation(summary = "Filtrar tareas", description = "Filtra por estado, prioridad y/o título.")
    @GetMapping("/filtrar")
    public ResponseEntity<ApiResponse<PageResponse<TareaDto>>> filtrarTareas(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Estado de la tarea") @RequestParam(required = false) Tarea.EstadoTarea estado,
            @Parameter(description = "Prioridad de la tarea") @RequestParam(required = false) Tarea.Prioridad prioridad,
            @Parameter(description = "Parte del título") @RequestParam(required = false) String titulo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/filtrar - Usuario ID: {}, Estado: {}, Prioridad: {}, Título: '{}'",
                usuarioId, estado, prioridad, titulo);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Tarea> tareaPage = tareaService.filtrarTareas(usuarioId, estado, titulo, prioridad, pageable);
        PageResponse<TareaDto> pageResponse = PageUtil.toPageResponse(tareaPage, tareaMapper::toDto);

        return ResponseEntity.ok(ApiResponse.success("Filtrado completado", pageResponse));
    }

    @Operation(summary = "Obtener tareas por etiqueta", description = "Lista de tareas asociadas a una etiqueta específica.")
    @GetMapping("/etiqueta/{etiquetaId}")
    public ResponseEntity<ApiResponse<PageResponse<TareaDto>>> obtenerTareasPorEtiqueta(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la etiqueta") @PathVariable Long etiquetaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/etiqueta/{} - Usuario ID: {}", etiquetaId, usuarioId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Tarea> tareaPage = tareaService.obtenerTareasPorEtiquetaId(etiquetaId, usuarioId, pageable);
        PageResponse<TareaDto> pageResponse = PageUtil.toPageResponse(tareaPage, tareaMapper::toDto);

        return ResponseEntity.ok(ApiResponse.success("Tareas obtenidas por etiqueta", pageResponse));
    }

    @Operation(summary = "Obtener tareas próximas a vencer", description = "Lista de tareas pendientes que vencen en los próximamente.")
    @GetMapping("/proximas-vencer")
    public ResponseEntity<ApiResponse<List<TareaDto>>> obtenerTareasProximasVencer(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Cantidad de días a futuro") @RequestParam(defaultValue = "7") int dias
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/proximas-vencer - Usuario ID: {}, Días: {}", usuarioId, dias);

        List<Tarea> tareas = tareaService.obtenerTareasProximasVencimiento(usuarioId, dias);
        List<TareaDto> tareasDto = tareas.stream()
                .map(tareaMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Tareas próximas a vencer obtenidas", tareasDto));
    }

    @Operation(summary = "Obtener estadísticas de tareas", description = "Conteo de tareas por estado")
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<Object>> obtenerEstadisticas(@AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/estadisticas - Usuario ID: {}", usuarioId);

        long pendientes = tareaService.contarTareasPorEstado(usuarioId, Tarea.EstadoTarea.PENDIENTE);
        long completadas = tareaService.contarTareasPorEstado(usuarioId, Tarea.EstadoTarea.COMPLETADA);
        long canceladas = tareaService.contarTareasPorEstado(usuarioId, Tarea.EstadoTarea.CANCELADA);
        long total = pendientes + completadas + canceladas;

        Map<String, Long> estadisticas = Map.of(
                "pendientes", pendientes,
                "completadas",completadas,
                "canceladas", canceladas,
                "total", total
        );

        return ResponseEntity.ok(ApiResponse.success("Estadísticas obtenidas", estadisticas));
    }

    @Operation(summary = "Obtener una tarea por ID", description = "Detalle completo de una tarea específica.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TareaDto>> obtenerTareaPorId(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la tarea") @PathVariable Long id
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/{} - Usuario ID: {}", id, usuarioId);

        Tarea tarea = tareaService.obtenerTareaPorIdYUsuarioId(id, usuarioId);
        TareaDto tareaDto = tareaMapper.toDto(tarea);

        return ResponseEntity.ok(ApiResponse.success("Tarea obtenida exitosamente", tareaDto));
    }

    @Operation(summary = "Crear nueva tarea", description = "Crea una tarea con estado PENDIENTE por defecto.")
    @PostMapping
    public ResponseEntity<ApiResponse<TareaDto>> crearTarea(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CrearTareaRequest request
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("POST /api/v1/tareas - Usuario ID: {}, Título: '{}'", usuarioId, request.getTitulo());

        var usuario = usuarioService.obtenerUsuarioPorId(usuarioId);

        Tarea tarea = Tarea.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad())
                .fechaVencimiento(request.getFechaVencimiento())
                .build();

        Tarea tareaCreada = tareaService.crearTarea(tarea, usuario);
        TareaDto tareaDto = tareaMapper.toDto(tareaCreada);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tarea creada exitosamente", tareaDto));
    }

    @Operation(summary = "Actualizar tarea", description = "Modifica los campos de una tarea existente.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TareaDto>> actualizarTarea(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la tarea") @PathVariable Long id,
            @Valid @RequestBody ActualizarTareaRequest request
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("PUT /api/v1/tareas/{} - Usuario ID: {}", id, usuarioId);

        Tarea tareaActualizada = Tarea.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad())
                .estado(request.getEstado())
                .fechaVencimiento(request.getFechaVencimiento() != null ?
                        LocalDateTime.parse(request.getFechaVencimiento(), DateTimeFormatter.ISO_DATE_TIME) : null)
                .build();

        Tarea tarea = tareaService.actualizarTarea(id, tareaActualizada, usuarioId);
        TareaDto tareaDto = tareaMapper.toDto(tarea);

        return ResponseEntity.ok(ApiResponse.success("Tarea actualizada exitosamente", tareaDto));
    }

    @Operation(summary = "Eliminar tarea", description = "Elimina una tarea y todas sus subtareas y adjuntos asociados.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarTarea(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la tarea") @PathVariable Long id
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("DELETE /api/v1/tareas/{} - Usuario ID: {}", id, usuarioId);

        tareaService.eliminarTarea(id, usuarioId);

        return ResponseEntity.ok(ApiResponse.success("Tarea eliminada exitosamente", null));
    }
}