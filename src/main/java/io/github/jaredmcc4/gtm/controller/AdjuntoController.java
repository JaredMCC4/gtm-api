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

/**
 * Controlador REST para gestionar archivos adjuntos asociados a tareas.
 * Permite subir, listar, descargar y eliminar adjuntos del usuario autenticado.
 */
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

    /**
     * Obtiene el ID de usuario autenticado a partir del JWT del principal o del header Authorization.
     *
     * @param jwt token JWT inyectado por Spring (puede ser null)
     * @param authorizationHeader header Authorization Bearer opcional
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
     * Sube un archivo y lo asocia a una tarea del usuario autenticado.
     *
     * @param tareaId identificador de la tarea
     * @param file archivo recibido via multipart
     * @param jwt JWT actual
     * @param authorizationHeader header Authorization Bearer opcional
     * @return adjunto creado en formato DTO
     */
    @Operation(summary = "Subir un archivo adjunto a una tarea")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Archivo subido",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Archivo invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada",
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

    /**
     * Lista los adjuntos de una tarea del usuario autenticado.
     *
     * @param tareaId identificador de la tarea
     * @param jwt JWT actual
     * @param authorizationHeader header Authorization Bearer opcional
     * @return lista de adjuntos en formato DTO
     */
    @Operation(summary = "Obtener adjuntos de una tarea", description = "Muestra todos los archivos adjuntos de una tarea.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Adjuntos obtenidos",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarea no encontrada",
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

    /**
     * Descarga un adjunto propiedad del usuario.
     *
     * @param adjuntoId identificador del adjunto
     * @param jwt JWT actual
     * @param authorizationHeader header Authorization Bearer opcional
     * @return recurso binario del adjunto con cabecera de descarga
     */
    @Operation(summary = "Descargar archivo adjunto", description = "Descarga el archivo seleccionado.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Archivo descargado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Adjunto no encontrado",
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

    /**
     * Elimina un adjunto del usuario autenticado.
     *
     * @param adjuntoId identificador del adjunto
     * @param jwt JWT actual
     * @param authorizationHeader header Authorization Bearer opcional
     * @return respuesta sin cuerpo tras eliminar
     */
    @Operation(summary = "Eliminar archivo adjunto", description = "Elimina un archivo del sistema y de la base de datos.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Adjunto eliminado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Adjunto no encontrado",
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

