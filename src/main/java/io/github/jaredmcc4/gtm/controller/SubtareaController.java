package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Subtarea;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.subtarea.SubtareaDto;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.SubtareaMapper;
import io.github.jaredmcc4.gtm.services.SubtareaService;
import io.github.jaredmcc4.gtm.util.JwtExtractorUtil;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/subtareas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Subtareas", description = "Gestión de subtareas.")
public class SubtareaController {

    private final SubtareaService subtareaService;
    private final SubtareaMapper subtareaMapper;
    private final JwtUtil jwtUtil;

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

    @Operation(summary = "Obtener subtareas de una tarea", description = "Muestra todas las subtareas de una tarea específica.")
    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<ApiResponse<List<SubtareaDto>>> obtenerSubtareasPorTarea(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la tarea") @PathVariable Long tareaId
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("GET /api/v1/subtareas/tarea/{} - Usuario ID: {}", tareaId, usuarioId);

        List<Subtarea> subtareas = subtareaService.mostrarSubtareas(tareaId, usuarioId);
        List<SubtareaDto> subtareasDto = subtareas.stream()
                .map(subtareaMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Subtareas obtenidas exitosamente", subtareasDto));
    }

    @Operation(summary = "Crear subtarea", description = "Agrega una nueva subtarea a una tarea existente.")
    @PostMapping("/tarea/{tareaId}")
    public ResponseEntity<ApiResponse<SubtareaDto>> crearSubtarea(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la tarea") @PathVariable Long tareaId,
            @Valid @RequestBody SubtareaDto subtareaDto
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("POST /api/v1/subtareas/tarea/{} - Usuario ID: {}, Título: '{}'",
                tareaId, usuarioId, subtareaDto.getTitulo());

        Subtarea subtarea = subtareaMapper.toEntity(subtareaDto);
        Subtarea subtareaCreada = subtareaService.crearSubtarea(tareaId, subtarea, usuarioId);
        SubtareaDto subtareaDtoCreada = subtareaMapper.toDto(subtareaCreada);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subtarea creada exitosamente", subtareaDtoCreada));
    }

    @Operation(summary = "Actualizar subtarea", description = "Modifica el título o estado de completado de una subtarea.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubtareaDto>> actualizarSubtarea(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la subtarea") @PathVariable Long id,
            @Valid @RequestBody SubtareaDto subtareaDto
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("PUT /api/v1/subtareas/{} - Usuario ID: {}", id, usuarioId);

        Subtarea subtareaActualizada = subtareaMapper.toEntity(subtareaDto);
        Subtarea subtarea = subtareaService.actualizarSubtarea(id, subtareaActualizada, usuarioId);
        SubtareaDto subtareaDtoActualizada = subtareaMapper.toDto(subtarea);

        return ResponseEntity.ok(ApiResponse.success("Subtarea actualizada exitosamente", subtareaDtoActualizada));
    }

    @Operation(summary = "Eliminar subtarea", description = "Elimina una subtarea elegida.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarSubtarea(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la subtarea") @PathVariable Long id
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("DELETE /api/v1/subtareas/{} - Usuario ID: {}", id, usuarioId);

        subtareaService.eliminarSubtarea(id, usuarioId);

        return ResponseEntity.ok(ApiResponse.success("Subtarea eliminada exitosamente", null));
    }
}