package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.dto.auth.JwtResponse;
import io.github.jaredmcc4.gtm.dto.auth.LoginRequest;
import io.github.jaredmcc4.gtm.dto.auth.RefreshTokenRequest;
import io.github.jaredmcc4.gtm.dto.auth.RegistroRequest;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.usuario.UsuarioDto;
import io.github.jaredmcc4.gtm.mapper.UsuarioMapper;
import io.github.jaredmcc4.gtm.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Endpoints publicos para registro, login, refresh y validacion de tokens JWT")
public class AuthController {

    private final AuthService authService;
    private final UsuarioMapper usuarioMapper;

    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta nueva.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email ya existe o datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<UsuarioDto>> registrarUsuario(@Valid @RequestBody RegistroRequest request) {
        log.info("POST /api/v1/auth/registro - Email: {}", request.getEmail());
        var usuario = authService.registrarUsuario(request);
        var usuarioDto = usuarioMapper.toDto(usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado exitosamente", usuarioDto));
    }

    @Operation(summary = "Iniciar sesion", description = "Autentica al usuario y devuelve JWT + Refresh Token.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticacion exitosa",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales invalidas o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - Email: {}", request.getEmail());
        var jwtResponse = authService.autenticarUsuario(request);
        return ResponseEntity.ok(ApiResponse.success("Autenticacion exitosa", jwtResponse));
    }

    @Operation(summary = "Refrescar token JWT", description = "Genera un nuevo JWT usando el Refresh Token valido.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refrescado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token invalido, revocado o expirado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/v1/auth/refresh");
        var jwtResponse = authService.refrescarToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refrescado exitosamente", jwtResponse));
    }

    @Operation(summary = "Cerrar sesion", description = "Revoca el refresh token del usuario.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sesion cerrada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Refresh token no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/v1/auth/logout");
        authService.cerrarSesion(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Sesion cerrada exitosamente", null));
    }

    @Operation(summary = "Validar token JWT", description = "Verifica si el token JWT proporcionado es valido.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token valido",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token invalido o expirado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/validar")
    public ResponseEntity<ApiResponse<Void>> validarToken(@RequestParam String token) {
        log.info("POST /api/v1/auth/validar");
        authService.validarToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token valido", null));
    }
}
