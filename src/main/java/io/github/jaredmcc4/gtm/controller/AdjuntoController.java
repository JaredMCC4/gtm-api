package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.dto.adjunto.AdjuntoDto;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.mapper.AdjuntoMapper;
import io.github.jaredmcc4.gtm.services.AdjuntoService;
import io.github.jaredmcc4.gtm.util.JwtExtractorUtil;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import io.github.jaredmcc4.gtm.validator.FileValidator;
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

    @PostMapping("/tarea/{tareaId}")
    @Operation(summary = "Subir un archivo adjunto a una tarea")
    public ResponseEntity<ApiResponse<AdjuntoDto>> subirAdjunto(
            @PathVariable Long tareaId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {

        Long usuarioId = JwtExtractorUtil.extractUsuarioId(jwt);
        log.info("Usuario {} subiendo adjunto a tarea {}", usuarioId, tareaId);

        FileValidator.validate(file);

        var adjunto = adjuntoService.subirAdjunto(tareaId, file, usuarioId);
        var dto = adjuntoMapper.toDto(adjunto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Archivo subido exitosamente", dto));
    }

    @Operation(summary = "Obtener adjuntos de una tarea", description = "Muestra todos los archivos adjuntos de una tarea.")
    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<ApiResponse<List<AdjuntoDto>>> obtenerAdjuntosPorTarea(
            @PathVariable Long tareaId,
            @AuthenticationPrincipal Jwt jwt) {

        Long usuarioId = JwtExtractorUtil.extractUsuarioId(jwt);
        log.info("Usuario {} obteniendo adjuntos de tarea {}", usuarioId, tareaId);

        var adjuntos = adjuntoService.mostrarAdjuntos(tareaId, usuarioId);
        var dtos = adjuntos.stream()
                .map(adjuntoMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Adjuntos recuperados exitosamente", dtos));
    }

    @Operation(summary = "Descargar archivo adjunto", description = "Descarga el archivo seleccionado.")
    @GetMapping("/{adjuntoId}/descargar")
    public ResponseEntity<Resource> descargarAdjunto(
            @PathVariable Long adjuntoId,
            @AuthenticationPrincipal Jwt jwt) {

        Long usuarioId = JwtExtractorUtil.extractUsuarioId(jwt);
        log.info("Usuario {} descargando adjunto {}", usuarioId, adjuntoId);

        Resource resource = adjuntoService.descargarAdjunto(adjuntoId, usuarioId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Eliminar archivo adjunto", description = "Elimina un archivo del sistema y de la base de datos.")
    @DeleteMapping("/{adjuntoId}")
    public ResponseEntity<ApiResponse<Void>> eliminarAdjunto(
            @PathVariable Long adjuntoId,
            @AuthenticationPrincipal Jwt jwt) {

        Long usuarioId = JwtExtractorUtil.extractUsuarioId(jwt);
        log.info("Usuario {} eliminando adjunto {}", usuarioId, adjuntoId);

        adjuntoService.eliminarAdjunto(adjuntoId, usuarioId);

        return ResponseEntity.ok(ApiResponse.success("Adjunto eliminado exitosamente", null));
    }
}