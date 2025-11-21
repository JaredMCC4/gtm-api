package io.github.jaredmcc4.gtm.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta generada al autenticar o refrescar tokens JWT.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta con los tokens de autenticación.")
public class JwtResponse {

    @Schema(description = "JWT de acceso firmado", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String jwtToken;

    @Builder.Default
    @Schema(description = "Tipo de token", example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "Tiempo de expiración del JWT en milisegundos", example = "3600000")
    private Long expiresIn;

    @Schema(description = "Refresh token emitido", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}

