package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.mapper.EtiquetaMapper;
import io.github.jaredmcc4.gtm.services.EtiquetaService;
import io.github.jaredmcc4.gtm.services.UsuarioService;
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
@RequestMapping("/api/v1/etiquetas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Etiquetas", description = "Gestión de etiquetas personalizadas para tareas.")
public class EtiquetaController {

    private final EtiquetaService etiquetaService;
    private final UsuarioService usuarioService;
    private final EtiquetaMapper etiquetaMapper;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Obtener todas las etiquetas del usuario", description = "Lista completa de etiquetas personalizadas.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EtiquetaDto>>> obtenerEtiquetas(@AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = jwtUtil.extraerUsuarioId(jwt.getTokenValue());
        log.info("GET /api/v1/etiquetas - Usuario ID: {}", usuarioId);

        List<Etiqueta> etiquetas = etiquetaService.obtenerEtiquetasPorUsuarioId(usuarioId);
        List<EtiquetaDto> etiquetasDto = etiquetas.stream()
                .map(etiquetaMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Etiquetas obtenidas exitosamente", etiquetasDto));
    }

    @Operation(summary = "Obtener etiqueta por ID", description = "Detalle de una etiqueta específica.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EtiquetaDto>> obtenerEtiquetaPorId(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la etiqueta") @PathVariable Long id
    ) {
        Long usuarioId = jwtUtil.extraerUsuarioId(jwt.getTokenValue());
        log.info("GET /api/v1/etiquetas/{} - Usuario ID: {}", id, usuarioId);

        Etiqueta etiqueta = etiquetaService.obtenerEtiquetaPorIdYUsuarioId(id, usuarioId);
        EtiquetaDto etiquetaDto = etiquetaMapper.toDto(etiqueta);

        return ResponseEntity.ok(ApiResponse.success("Etiqueta obtenida exitosamente", etiquetaDto));
    }

    @Operation(summary = "Crear nueva etiqueta", description = "Crea una etiqueta con nombre y color hexadecimal únicos.")
    @PostMapping
    public ResponseEntity<ApiResponse<EtiquetaDto>> crearEtiqueta(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody EtiquetaDto etiquetaDto
    ) {
        Long usuarioId = jwtUtil.extraerUsuarioId(jwt.getTokenValue());
        log.info("POST /api/v1/etiquetas - Usuario ID: {}, Nombre: '{}'", usuarioId, etiquetaDto.getNombre());

        var usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        Etiqueta etiqueta = etiquetaMapper.toEntity(etiquetaDto);

        Etiqueta etiquetaCreada = etiquetaService.crearEtiqueta(etiqueta, usuario);
        EtiquetaDto etiquetaDtoCreada = etiquetaMapper.toDto(etiquetaCreada);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Etiqueta creada exitosamente", etiquetaDtoCreada));
    }

    @Operation(summary = "Actualizar etiqueta", description = "Modifica el nombre y color de una etiqueta.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EtiquetaDto>> actualizarEtiqueta(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la etiqueta") @PathVariable Long id,
            @Valid @RequestBody EtiquetaDto etiquetaDto
    ) {
        Long usuarioId = jwtUtil.extraerUsuarioId(jwt.getTokenValue());
        log.info("PUT /api/v1/etiquetas/{} - Usuario ID: {}", id, usuarioId);

        Etiqueta etiquetaActualizada = etiquetaMapper.toEntity(etiquetaDto);
        Etiqueta etiqueta = etiquetaService.actualizarEtiqueta(id, etiquetaActualizada, usuarioId);
        EtiquetaDto etiquetaDtoActualizada = etiquetaMapper.toDto(etiqueta);

        return ResponseEntity.ok(ApiResponse.success("Etiqueta actualizada exitosamente", etiquetaDtoActualizada));
    }

    @Operation(summary = "Eliminar etiqueta", description = "Elimina una etiqueta y la desvincula de todas las tareas.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarEtiqueta(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID de la etiqueta") @PathVariable Long id
    ) {
        Long usuarioId = jwtUtil.extraerUsuarioId(jwt.getTokenValue());
        log.info("DELETE /api/v1/etiquetas/{} - Usuario ID: {}", id, usuarioId);

        etiquetaService.eliminarEtiqueta(id, usuarioId);

        return ResponseEntity.ok(ApiResponse.success("Etiqueta eliminada exitosamente", null));
    }
}