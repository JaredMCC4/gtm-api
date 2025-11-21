package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Subtarea;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.response.ErrorResponse;
import io.github.jaredmcc4.gtm.dto.subtarea.SubtareaDto;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.SubtareaMapper;
import io.github.jaredmcc4.gtm.services.SubtareaService;
import io.github.jaredmcc4.gtm.util.JwtExtractorUtil;
import io.github.jaredmcc4.gtm.util.JwtUtil;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestion de subtareas asociadas a tareas del usuario autenticado.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/subtareas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Subtareas", description = "Gestion de subtareas.")
public class SubtareaController {

    private final SubtareaService subtareaService;
    private final SubtareaMapper subtareaMapper;
    private final JwtUtil jwtUtil;

    /**
     * Resuelve el ID de usuario autenticado desde el JWT recibido o el header Authorization.
     *
     * @param jwt token JWT inyectado por Spring Security (puede ser null)
     * @param authorizationHeader encabezado Authorization con formato Bearer (opcional)
     * @return identificador interno del usuario
     * @throws UnauthorizedException si no se puede determinar el usuario
     */
    private Long resolveUsuarioId(Jwt jwt, String authorizationHeader) {
        if (jwt != null) {
            return JwtExtractorUtil.extractUsuarioId(jwt);
        }
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return jwtUtil.extraerUsuarioId(token);
        }
        throw new UnauthorizedException("No se pudo determinar el usuario autenticado");
    }

    /**
     * Obtiene todas las subtareas de una tarea especifica para el usuario autenticado.
     *
     * @param jwt JWT actual
     * @param authorizationHeader header Authorization con Bearer token (opcional)
     * @param tareaId identificador de la tarea
     * @return lista de subtareas asociadas
     */
    @Operation(
            summary = "Obtener subtareas de una tarea",
            description = "Muestra todas las subtareas de una tarea especifica."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subtareas obtenidas",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<ApiResponse<List<SubtareaDto>>> obtenerSubtareasPorTarea(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la tarea", example = "10") @PathVariable Long tareaId
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("GET /api/v1/subtareas/tarea/{} - Usuario ID: {}", tareaId, usuarioId);

        List<Subtarea> subtareas = subtareaService.mostrarSubtareas(tareaId, usuarioId);
        List<SubtareaDto> subtareasDto = subtareas.stream()
                .map(subtareaMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Subtareas obtenidas exitosamente", subtareasDto));
    }

    /**
     * Crea una nueva subtarea bajo una tarea existente.
     *
     * @param jwt JWT actual
     * @param authorizationHeader header Authorization con Bearer token (opcional)
     * @param tareaId identificador de la tarea padre
     * @param subtareaDto datos de la subtarea a crear
     * @return subtarea creada
     */
    @Operation(
            summary = "Crear subtarea",
            description = "Agrega una nueva subtarea a una tarea existente."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Subtarea creada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/tarea/{tareaId}")
    public ResponseEntity<ApiResponse<SubtareaDto>> crearSubtarea(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la tarea", example = "10") @PathVariable Long tareaId,
            @Valid @RequestBody SubtareaDto subtareaDto
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("POST /api/v1/subtareas/tarea/{} - Usuario ID: {}, Titulo: '{}'",
                tareaId, usuarioId, subtareaDto.getTitulo());

        Subtarea subtarea = subtareaMapper.toEntity(subtareaDto);
        Subtarea subtareaCreada = subtareaService.crearSubtarea(tareaId, subtarea, usuarioId);
        SubtareaDto subtareaDtoCreada = subtareaMapper.toDto(subtareaCreada);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subtarea creada exitosamente", subtareaDtoCreada));
    }

    /**
     * Actualiza titulo o estado de una subtarea del usuario autenticado.
     *
     * @param jwt JWT actual
     * @param authorizationHeader header Authorization con Bearer token (opcional)
     * @param id identificador de la subtarea
     * @param subtareaDto datos a actualizar
     * @return subtarea actualizada
     */
    @Operation(
            summary = "Actualizar subtarea",
            description = "Modifica el titulo o estado de completado de una subtarea."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subtarea actualizada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Subtarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubtareaDto>> actualizarSubtarea(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la subtarea", example = "15") @PathVariable Long id,
            @Valid @RequestBody SubtareaDto subtareaDto
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("PUT /api/v1/subtareas/{} - Usuario ID: {}", id, usuarioId);

        Subtarea subtareaActualizada = subtareaMapper.toEntity(subtareaDto);
        Subtarea subtarea = subtareaService.actualizarSubtarea(id, subtareaActualizada, usuarioId);
        SubtareaDto subtareaDtoActualizada = subtareaMapper.toDto(subtarea);

        return ResponseEntity.ok(ApiResponse.success("Subtarea actualizada exitosamente", subtareaDtoActualizada));
    }

    /**
     * Elimina una subtarea del usuario autenticado.
     *
     * @param jwt JWT actual
     * @param authorizationHeader header Authorization con Bearer token (opcional)
     * @param id identificador de la subtarea
     * @return respuesta sin datos si la eliminacion fue exitosa
     */
    @Operation(
            summary = "Eliminar subtarea",
            description = "Elimina una subtarea."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subtarea eliminada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Subtarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarSubtarea(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la subtarea", example = "15") @PathVariable Long id
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("DELETE /api/v1/subtareas/{} - Usuario ID: {}", id, usuarioId);

        subtareaService.eliminarSubtarea(id, usuarioId);

        return ResponseEntity.ok(ApiResponse.success("Subtarea eliminada exitosamente", null));
    }
}

