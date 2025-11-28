package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.dto.auth.JwtResponse;
import io.github.jaredmcc4.gtm.dto.auth.LoginRequest;
import io.github.jaredmcc4.gtm.dto.auth.RefreshTokenRequest;
import io.github.jaredmcc4.gtm.dto.auth.RegistroRequest;
import io.github.jaredmcc4.gtm.dto.auth.SocialLoginRequest;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.usuario.UsuarioDto;
import io.github.jaredmcc4.gtm.mapper.UsuarioMapper;
import io.github.jaredmcc4.gtm.services.AuthService;
import io.github.jaredmcc4.gtm.services.SocialAuthService;
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
    private final SocialAuthService socialAuthService;
    private final UsuarioMapper usuarioMapper;

    /**
     * Registra un nuevo usuario final y devuelve sus datos visibles.
     *
     * @param request datos de registro validados (email, password, nombre, zona horaria)
     * @return respuesta con el usuario creado en formato DTO
     */
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

    /**
     * Autentica al usuario y entrega JWT de acceso y refresh token.
     *
     * @param request credenciales de acceso (email y password)
     * @return tokens emitidos y sus expiraciones
     */
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

    /**
     * Intercambia el authorization code o access token de un proveedor externo y emite JWT propios.
     *
     * @param request datos del proveedor y token/código
     * @return tokens de la plataforma
     */
    @Operation(summary = "Login con Google, Microsoft o GitHub", description = "Intercambia el authorization code o access token y devuelve JWT + refresh propios.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticacion social exitosa",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o configuracion faltante",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/oauth/login")
    public ResponseEntity<ApiResponse<JwtResponse>> loginConProveedor(@Valid @RequestBody SocialLoginRequest request) {
        log.info("POST /api/v1/auth/oauth/login - Provider: {}", request.getProvider());
        var jwtResponse = socialAuthService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Autenticacion exitosa", jwtResponse));
    }

    /**
     * Genera un nuevo par de tokens a partir de un refresh token valido.
     *
     * @param request request con el refresh token actual
     * @return nuevo JWT de acceso y refresh token renovado
     */
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

    /**
     * Revoca el refresh token proporcionado y termina la sesion del usuario.
     *
     * @param request request con el refresh token a revocar
     * @return respuesta sin cuerpo indicando exito
     */
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

    /**
     * Valida la integridad y vigencia de un JWT de acceso.
     *
     * @param token JWT a validar
     * @return respuesta sin cuerpo si el token es valido
     */
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
