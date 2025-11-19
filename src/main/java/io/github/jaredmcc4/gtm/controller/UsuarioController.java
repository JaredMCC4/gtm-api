package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.usuario.ActualizarUsuarioRequest;
import io.github.jaredmcc4.gtm.dto.usuario.CambiarPasswordRequest;
import io.github.jaredmcc4.gtm.dto.usuario.UsuarioDto;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.UsuarioMapper;
import io.github.jaredmcc4.gtm.services.UsuarioService;
import io.github.jaredmcc4.gtm.util.JwtExtractorUtil;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Gestión del perfil de usuario.")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final JwtUtil jwtUtil;

    private Long resolverUsuarioId(Jwt jwt, String authorizationHeader) {
        if (jwt != null) {
            return JwtExtractorUtil.extractUsuarioId(jwt);
        }
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return jwtUtil.extraerUsuarioId(token);
        }
        throw new UnauthorizedException("No se pudo determinar el usuario autenticado");
    }

    @Operation(summary = "Obtener perfil del usuario autenticado", description = "Información completa del usuario.")
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioDto>> obtenerPerfil(@AuthenticationPrincipal Jwt jwt,
                                                                 @RequestHeader(value = "Authorization", required = false) String
            authorizationHeader) {
        Long usuarioId = resolverUsuarioId(jwt, authorizationHeader);
        log.info("GET /api/v1/usuarios/perfil - Usuario ID: {}", usuarioId);

        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        UsuarioDto usuarioDto = usuarioMapper.toDto(usuario);

        return ResponseEntity.ok(ApiResponse.success("Perfil obtenido exitosamente", usuarioDto));
    }

    @Operation(summary = "Actualizar perfil de usuario", description = "Modificar nombre y zona horaria.")
    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioDto>> actualizarPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String
                    authorizationHeader,
            @Valid @RequestBody ActualizarUsuarioRequest request
    ) {
        Long usuarioId = resolverUsuarioId(jwt, authorizationHeader);
        log.info("PUT /api/v1/usuarios/perfil - Usuario ID: {}", usuarioId);

        Usuario datosActualizados = Usuario.builder()
                .nombreUsuario(request.getNombreUsuario())
                .zonaHoraria(request.getZonaHoraria())
                .build();

        Usuario usuario = usuarioService.actualizarUsuario(usuarioId, datosActualizados);
        UsuarioDto usuarioDto = usuarioMapper.toDto(usuario);

        return ResponseEntity.ok(ApiResponse.success("Perfil actualizado exitosamente", usuarioDto));
    }

    @Operation(summary = "Cambiar contraseña", description = "Modifica la contraseña del usuario autenticado.")
    @PatchMapping("/cambiar-password")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String
                    authorizationHeader,
            @Valid @RequestBody CambiarPasswordRequest request
    ) {
        Long usuarioId = resolverUsuarioId(jwt, authorizationHeader);
        log.info("PATCH /api/v1/usuarios/cambiar-password - Usuario ID: {}", usuarioId);

        usuarioService.cambiarPassword(usuarioId, request.getPasswordActual(), request.getPasswordNueva());

        return ResponseEntity.ok(ApiResponse.success("Contraseña cambiada exitosamente", null));
    }
}