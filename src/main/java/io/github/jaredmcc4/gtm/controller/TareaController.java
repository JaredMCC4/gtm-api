package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.response.ErrorResponse;
import io.github.jaredmcc4.gtm.dto.response.PageResponse;
import io.github.jaredmcc4.gtm.dto.tarea.CrearTareaRequest;
import io.github.jaredmcc4.gtm.dto.tarea.ActualizarTareaRequest;
import io.github.jaredmcc4.gtm.dto.tarea.EstadisticasDto;
import io.github.jaredmcc4.gtm.dto.tarea.TareaDto;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.TareaMapper;
import io.github.jaredmcc4.gtm.services.TareaService;
import io.github.jaredmcc4.gtm.services.UsuarioService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import io.github.jaredmcc4.gtm.util.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestion completa de tareas del usuario autenticado.
 * Expone operaciones de listado, filtrado, estadisticas, creacion, actualizacion y eliminacion.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tareas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tareas", description = "Gestion de todas las tareas del usuario")
public class TareaController {

    private final TareaService tareaService;
    private final UsuarioService usuarioService;
    private final TareaMapper tareaMapper;
    private final JwtUtil jwtUtil;

    /**
     * Obtiene el ID del usuario autenticado a partir del JWT actual.
     * Lee el token del principal o del contexto de seguridad y valida su presencia.
     *
     * @param jwt token extraido por {@link AuthenticationPrincipal} (puede ser null)
     * @return identificador interno del usuario autenticado
     * @throws UnauthorizedException si no hay JWT valido en el contexto
     */
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

    /**
     * Devuelve una pagina de tareas del usuario autenticado con soporte de orden y filtros basicos.
     *
     * @param jwt JWT actual
     * @param page numero de pagina (0-based)
     * @param size tamano de pagina
     * @param sortBy campo para ordenar
     * @param direction direccion de orden (ASC/DESC)
     * @param estado filtro opcional por estado
     * @param search texto a buscar en titulo o descripcion
     * @return pagina de tareas del usuario
     */
    @Operation(
            summary = "Obtener todas las tareas",
            description = "Lista paginada de tareas del usuario, ordenadas por fecha de creacion (DESC por defecto)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tareas obtenidas",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TareaDto>>> obtenerTareas(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Numero de pagina (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamano de pagina", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenar", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direccion de orden", example = "DESC") @RequestParam(defaultValue = "DESC") String direction,
            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) Tarea.EstadoTarea estado,
            @Parameter(description = "Texto a buscar en titulo/descripcion") @RequestParam(required = false) String search
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

    /**
     * Busca tareas por texto en titulo y descripcion para el usuario autenticado.
     *
     * @param jwt JWT actual
     * @param texto texto a buscar
     * @param page numero de pagina (0-based)
     * @param size tamano de pagina
     * @return pagina con tareas que coinciden con el texto
     */
    @Operation(
            summary = "Buscar tareas por texto",
            description = "Busqueda en titulo y descripcion de las tareas del usuario."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Busqueda completada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<PageResponse<TareaDto>>> buscarTareas(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Texto a buscar", example = "login") @RequestParam String texto,
            @Parameter(description = "Numero de pagina (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamano de pagina", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/buscar - Usuario ID: {}, Texto: '{}'", usuarioId, texto);

        Pageable pageable = PageRequest.of(page, size);
        Page<Tarea> tareaPage = tareaService.buscarTareasPorTexto(usuarioId, texto, pageable);
        PageResponse<TareaDto> pageResponse = PageUtil.toPageResponse(tareaPage, tareaMapper::toDto);

        return ResponseEntity.ok(ApiResponse.success("Busqueda completada", pageResponse));
    }

    /**
     * Filtra tareas por estado, prioridad y titulo parcial.
     *
     * @param jwt JWT actual
     * @param estado estado opcional
     * @param prioridad prioridad opcional
     * @param titulo fragmento del titulo opcional
     * @param page numero de pagina (0-based)
     * @param size tamano de pagina
     * @return pagina de tareas filtradas
     */
    @Operation(
            summary = "Filtrar tareas",
            description = "Filtra por estado, prioridad y/o titulo."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Filtro aplicado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/filtrar")
    public ResponseEntity<ApiResponse<PageResponse<TareaDto>>> filtrarTareas(
        @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "Estado de la tarea") @RequestParam(required = false) Tarea.EstadoTarea estado,
        @Parameter(description = "Prioridad de la tarea") @RequestParam(required = false) Tarea.Prioridad prioridad,
        @Parameter(description = "Parte del titulo") @RequestParam(required = false) String titulo,
        @Parameter(description = "Numero de pagina (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamano de pagina", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/filtrar - Usuario ID: {}, Estado: {}, Prioridad: {}, Titulo: '{}'",
                usuarioId, estado, prioridad, titulo);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Tarea> tareaPage = tareaService.filtrarTareas(usuarioId, estado, titulo, prioridad, pageable);
        PageResponse<TareaDto> pageResponse = PageUtil.toPageResponse(tareaPage, tareaMapper::toDto);

        return ResponseEntity.ok(ApiResponse.success("Filtrado completado", pageResponse));
    }

    /**
     * Obtiene tareas asociadas a una etiqueta especifica para el usuario autenticado.
     *
     * @param jwt JWT actual
     * @param etiquetaId identificador de la etiqueta
     * @param page numero de pagina (0-based)
     * @param size tamano de pagina
     * @return pagina de tareas vinculadas a la etiqueta
     */
    @Operation(
            summary = "Obtener tareas por etiqueta",
            description = "Lista de tareas asociadas a una etiqueta especifica."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tareas encontradas",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Etiqueta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/etiqueta/{etiquetaId}")
    public ResponseEntity<ApiResponse<PageResponse<TareaDto>>> obtenerTareasPorEtiqueta(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la etiqueta", example = "5") @PathVariable Long etiquetaId,
            @Parameter(description = "Numero de pagina (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamano de pagina", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/etiqueta/{} - Usuario ID: {}", etiquetaId, usuarioId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Tarea> tareaPage = tareaService.obtenerTareasPorEtiquetaId(etiquetaId, usuarioId, pageable);
        PageResponse<TareaDto> pageResponse = PageUtil.toPageResponse(tareaPage, tareaMapper::toDto);

        return ResponseEntity.ok(ApiResponse.success("Tareas obtenidas por etiqueta", pageResponse));
    }

    /**
     * Lista tareas pendientes que vencen dentro de los proximos N dias.
     *
     * @param jwt JWT actual
     * @param dias ventana de dias a futuro
     * @return lista de tareas proximas a vencer
     */
    @Operation(
            summary = "Obtener tareas proximas a vencer",
            description = "Lista de tareas PENDIENTE que vencen en los proximos N dias."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/proximas-vencer")
    public ResponseEntity<ApiResponse<List<TareaDto>>> obtenerTareasProximasVencer(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Cantidad de dias a futuro", example = "7") @RequestParam(defaultValue = "7") int dias
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/proximas-vencer - Usuario ID: {}, Dias: {}", usuarioId, dias);

        List<Tarea> tareas = tareaService.obtenerTareasProximasVencimiento(usuarioId, dias);
        List<TareaDto> tareasDto = tareas.stream().map(tareaMapper::toDto).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Tareas proximas a vencer obtenidas", tareasDto));
    }

    /**
     * Devuelve estadisticas de conteo de tareas por estado para el usuario autenticado.
     *
     * @param jwt JWT actual
     * @return totales de pendientes, completadas, canceladas y total
     */
    @Operation(
            summary = "Obtener estadisticas de tareas",
            description = "Conteo de tareas por estado para el usuario."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estadisticas obtenidas",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<EstadisticasDto>> obtenerEstadisticas(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/estadisticas - Usuario ID: {}", usuarioId);

        long pendientes = tareaService.contarTareasPorEstado(usuarioId, Tarea.EstadoTarea.PENDIENTE);
        long completadas = tareaService.contarTareasPorEstado(usuarioId, Tarea.EstadoTarea.COMPLETADA);
        long canceladas = tareaService.contarTareasPorEstado(usuarioId, Tarea.EstadoTarea.CANCELADA);
        long total = pendientes + completadas + canceladas;

        EstadisticasDto estadisticas = EstadisticasDto.builder()
                .pendientes(pendientes)
                .completadas(completadas)
                .canceladas(canceladas)
                .total(total)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Estadisticas obtenidas", estadisticas));
    }

    /**
     * Obtiene el detalle de una tarea especifica perteneciente al usuario autenticado.
     *
     * @param jwt JWT actual
     * @param id identificador de la tarea
     * @return tarea en formato DTO
     */
    @Operation(
            summary = "Obtener una tarea por ID",
            description = "Detalle completo de una tarea."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tarea encontrada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TareaDto>> obtenerTareaPorId(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la tarea", example = "10") @PathVariable Long id
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("GET /api/v1/tareas/{} - Usuario ID: {}", id, usuarioId);

        Tarea tarea = tareaService.obtenerTareaPorIdYUsuarioId(id, usuarioId);
        TareaDto tareaDto = tareaMapper.toDto(tarea);

        return ResponseEntity.ok(ApiResponse.success("Tarea obtenida exitosamente", tareaDto));
    }

    /**
     * Crea una nueva tarea (estado PENDIENTE) para el usuario autenticado.
     *
     * @param jwt JWT actual
     * @param request datos de la tarea a crear
     * @return tarea creada
     */
    @Operation(
            summary = "Crear nueva tarea",
            description = "Crea una tarea con estado PENDIENTE por defecto."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tarea creada",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TareaDto>> crearTarea(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CrearTareaRequest request
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("POST /api/v1/tareas - Usuario ID: {}, Titulo: '{}'", usuarioId, request.getTitulo());

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

    /**
     * Actualiza una tarea existente del usuario autenticado.
     *
     * @param jwt JWT actual
     * @param id identificador de la tarea
     * @param request datos de actualizacion
     * @return tarea actualizada
     */
    @Operation(
            summary = "Actualizar tarea",
            description = "Modifica los campos de una tarea existente."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tarea actualizada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TareaDto>> actualizarTarea(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la tarea", example = "10") @PathVariable Long id,
            @Valid @RequestBody ActualizarTareaRequest request
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("PUT /api/v1/tareas/{} - Usuario ID: {}", id, usuarioId);

        Tarea tareaActualizada = Tarea.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad())
                .estado(request.getEstado())
                .fechaVencimiento(request.getFechaVencimiento())
                .build();

        Tarea tarea = tareaService.actualizarTarea(id, tareaActualizada, usuarioId);
        TareaDto tareaDto = tareaMapper.toDto(tarea);

        return ResponseEntity.ok(ApiResponse.success("Tarea actualizada exitosamente", tareaDto));
    }

    /**
     * Elimina una tarea y sus recursos asociados para el usuario autenticado.
     *
     * @param jwt JWT actual
     * @param id identificador de la tarea
     * @return respuesta sin datos cuando la eliminacion es exitosa
     */
    @Operation(
            summary = "Eliminar tarea",
            description = "Elimina una tarea y todas sus subtareas y adjuntos asociados."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tarea eliminada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarTarea(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la tarea", example = "10") @PathVariable Long id
    ) {
        Long usuarioId = resolverUsuarioId(jwt);
        log.info("DELETE /api/v1/tareas/{} - Usuario ID: {}", id, usuarioId);

        tareaService.eliminarTarea(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Tarea eliminada exitosamente", null));
    }
}

