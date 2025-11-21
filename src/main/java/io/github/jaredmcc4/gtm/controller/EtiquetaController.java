package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.response.ErrorResponse;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.EtiquetaMapper;
import io.github.jaredmcc4.gtm.services.EtiquetaService;
import io.github.jaredmcc4.gtm.services.UsuarioService;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/etiquetas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Etiquetas", description = "Gestion de etiquetas personalizadas para tareas.")
public class EtiquetaController {

    private final EtiquetaService etiquetaService;
    private final EtiquetaMapper etiquetaMapper;
    private final UsuarioService usuarioService;
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

    @Operation(summary = "Obtener todas las etiquetas del usuario", description = "Lista completa de etiquetas personalizadas.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Etiquetas obtenidas",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<EtiquetaDto>>> obtenerEtiquetas(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("GET /api/v1/etiquetas - Usuario ID: {}", usuarioId);

        List<Etiqueta> etiquetas = etiquetaService.obtenerEtiquetasPorUsuarioId(usuarioId);
        List<EtiquetaDto> etiquetasDto = etiquetas.stream()
                .map(etiquetaMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Etiquetas obtenidas exitosamente", etiquetasDto));
    }

    @Operation(summary = "Obtener etiqueta por ID", description = "Detalle de una etiqueta especifica.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Etiqueta encontrada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Etiqueta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EtiquetaDto>> obtenerEtiquetaPorId(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la etiqueta", example = "3") @PathVariable Long id
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("GET /api/v1/etiquetas/{} - Usuario ID: {}", id, usuarioId);

        Etiqueta etiqueta = etiquetaService.obtenerEtiquetaPorIdYUsuarioId(id, usuarioId);
        EtiquetaDto etiquetaDto = etiquetaMapper.toDto(etiqueta);

        return ResponseEntity.ok(ApiResponse.success("Etiqueta obtenida exitosamente", etiquetaDto));
    }

    @Operation(summary = "Crear nueva etiqueta", description = "Crea una etiqueta con nombre y color hexadecimal unicos.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Etiqueta creada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos / duplicados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<EtiquetaDto>> crearEtiqueta(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody EtiquetaDto etiquetaDto
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("POST /api/v1/etiquetas - Usuario ID: {}, Nombre: '{}'", usuarioId, etiquetaDto.getNombre());

        var usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        Etiqueta etiqueta = etiquetaMapper.toEntity(etiquetaDto);

        Etiqueta etiquetaCreada = etiquetaService.crearEtiqueta(etiqueta, usuario);
        EtiquetaDto etiquetaDtoCreada = etiquetaMapper.toDto(etiquetaCreada);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Etiqueta creada exitosamente", etiquetaDtoCreada));
    }

    @Operation(summary = "Actualizar etiqueta", description = "Modifica el nombre y color de una etiqueta.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Etiqueta actualizada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos / duplicados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Etiqueta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EtiquetaDto>> actualizarEtiqueta(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la etiqueta", example = "3") @PathVariable Long id,
            @Valid @RequestBody EtiquetaDto etiquetaDto
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("PUT /api/v1/etiquetas/{} - Usuario ID: {}", id, usuarioId);

        Etiqueta etiquetaActualizada = etiquetaMapper.toEntity(etiquetaDto);
        Etiqueta etiqueta = etiquetaService.actualizarEtiqueta(id, etiquetaActualizada, usuarioId);
        EtiquetaDto etiquetaDtoActualizada = etiquetaMapper.toDto(etiqueta);

        return ResponseEntity.ok(ApiResponse.success("Etiqueta actualizada exitosamente", etiquetaDtoActualizada));
    }

    @Operation(summary = "Eliminar etiqueta", description = "Elimina una etiqueta y la desvincula de todas las tareas.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Etiqueta eliminada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Etiqueta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarEtiqueta(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "ID de la etiqueta", example = "3") @PathVariable Long id
    ) {
        Long usuarioId = resolveUsuarioId(jwt, authorizationHeader);
        log.info("DELETE /api/v1/etiquetas/{} - Usuario ID: {}", id, usuarioId);

        etiquetaService.eliminarEtiqueta(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.success("Etiqueta eliminada exitosamente", null));
    }
}
