package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.dto.adjunto.AdjuntoDto;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.response.ErrorResponse;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.AdjuntoMapper;
import io.github.jaredmcc4.gtm.services.AdjuntoService;
import io.github.jaredmcc4.gtm.util.JwtExtractorUtil;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import io.github.jaredmcc4.gtm.validator.FileValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Adjuntos", description = "Gestion de adjuntos en tareas.")
public class AdjuntoController {

    private final AdjuntoService adjuntoService;
    private final AdjuntoMapper adjuntoMapper;
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

    @Operation(summary = "Subir un archivo adjunto a una tarea")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Archivo subido",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Archivo invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/tarea/{tareaId}")
    public ResponseEntity<ApiResponse<AdjuntoDto>> subirAdjunto(
            @Parameter(description = "ID de la tarea", example = "10") @PathVariable Long tareaId,
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("Usuario {} subiendo adjunto a tarea {}", usuarioId, tareaId);

        FileValidator.validate(file);

        var adjunto = adjuntoService.subirAdjunto(tareaId, file, usuarioId);
        var dto = adjuntoMapper.toDto(adjunto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Archivo subido exitosamente", dto));
    }

    @Operation(summary = "Obtener adjuntos de una tarea", description = "Muestra todos los archivos adjuntos de una tarea.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adjuntos obtenidos",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<ApiResponse<List<AdjuntoDto>>> obtenerAdjuntosPorTarea(
            @Parameter(description = "ID de la tarea", example = "10") @PathVariable Long tareaId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("Usuario {} obteniendo adjuntos de tarea {}", usuarioId, tareaId);

        var adjuntos = adjuntoService.mostrarAdjuntos(tareaId, usuarioId);
        var dtos = adjuntos.stream()
                .map(adjuntoMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Adjuntos recuperados exitosamente", dtos));
    }

    @Operation(summary = "Descargar archivo adjunto", description = "Descarga el archivo seleccionado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo descargado"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Adjunto no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{adjuntoId}/descargar")
    public ResponseEntity<Resource> descargarAdjunto(
            @Parameter(description = "ID del adjunto", example = "5") @PathVariable Long adjuntoId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("Usuario {} descargando adjunto {}", usuarioId, adjuntoId);

        Adjunto adjunto = adjuntoService.obtenerAdjuntoPorId(adjuntoId, usuarioId);
        Resource resource = adjuntoService.descargarAdjunto(adjuntoId, usuarioId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(adjunto.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Eliminar archivo adjunto", description = "Elimina un archivo del sistema y de la base de datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adjunto eliminado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Adjunto no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{adjuntoId}")
    public ResponseEntity<ApiResponse<Void>> eliminarAdjunto(
            @Parameter(description = "ID del adjunto", example = "5") @PathVariable Long adjuntoId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("Usuario {} eliminando adjunto {}", usuarioId, adjuntoId);

        adjuntoService.eliminarAdjunto(adjuntoId, usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Adjunto eliminado exitosamente", null));
    }
}
