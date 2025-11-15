package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.dto.adjunto.AdjuntoDto;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.mapper.AdjuntoMapper;
import io.github.jaredmcc4.gtm.services.AdjuntoService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/adjuntos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Adjuntos", description = "Gestión de adjuntos en tareas.")
public class AdjuntoController {

    private final AdjuntoService adjuntoService;
    private final AdjuntoMapper adjuntoMapper;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Subir archivo adjunto", description = "Sube un archivo y lo vincula a una tarea (máx 10MB).")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
    )
    @PostMapping(value = "/tarea/{tareaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdjuntoDto>> subirAdjunto(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la tarea.") @PathVariable Long tareaId,
            @Parameter(description = "Archivo a subir.") @RequestParam("file") MultipartFile file
    ) {
        Long usuarioId = jwtUtil.extraerUsuarioId(jwt.getTokenValue());
        log.info("POST /api/v1/adjuntos/tarea/{} - Usuario ID: {}, Archivo: '{}'",
                tareaId, usuarioId, file.getOriginalFilename());

        Adjunto adjunto = adjuntoService.subirAdjunto(tareaId, file, usuarioId);
        AdjuntoDto adjuntoDto = adjuntoMapper.toDto(adjunto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Archivo subido exitosamente", adjuntoDto));
    }

    @Operation(summary = "Obtener adjuntos de una tarea", description = "Muestra todos los archivos adjuntos de una tarea.")
    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<ApiResponse<List<AdjuntoDto>>> obtenerAdjuntosPorTarea(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la tarea") @PathVariable Long tareaId
    ) {
        Long usuarioId = jwtUtil.extraerUsuarioId(jwt.getTokenValue());
        log.info("GET /api/v1/adjuntos/tarea/{} - Usuario ID: {}", tareaId, usuarioId);

        List<Adjunto> adjuntos = adjuntoService.mostrarAdjuntos(tareaId, usuarioId);
        List<AdjuntoDto> adjuntosDto = adjuntos.stream()
                .map(adjuntoMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Adjuntos obtenidos exitosamente", adjuntosDto));
    }

    @Operation(summary = "Descargar archivo adjunto", description = "Descarga el archivo seleccionado.")
    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargarAdjunto(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del adjunto") @PathVariable Long id
    ) {
        Long usuarioId = jwtUtil.extraerUsuarioId(jwt.getTokenValue());
        log.info("GET /api/v1/adjuntos/{}/descargar - Usuario ID: {}", id, usuarioId);

        Resource resource = adjuntoService.descargarAdjunto(id, usuarioId);
        Adjunto adjunto = adjuntoService.obtenerAdjuntoPorId(id, usuarioId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(adjunto.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + adjunto.getNombre() + "\"")
                .body(resource);
    }

    @Operation(summary = "Eliminar archivo adjunto", description = "Elimina un archivo del sistema y de la base de datos.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarAdjunto(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID del adjunto") @PathVariable Long id
    ) {
        Long usuarioId = jwtUtil.extraerUsuarioId(jwt.getTokenValue());
        log.info("DELETE /api/v1/adjuntos/{} - Usuario ID: {}", id, usuarioId);

        adjuntoService.eliminarAdjunto(id, usuarioId);

        return ResponseEntity.ok(ApiResponse.success("Adjunto eliminado exitosamente", null));
    }
}