package io.github.jaredmcc4.gtm.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload para refrescar o revocar JWT usando el refresh token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para refrescar o revocar un JWT usando el refresh token.")
public class RefreshTokenRequest {

    @Schema(description = "Refresh token emitido previamente", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotBlank(message = "El refresh token no puede estar vacio.")
    private String refreshToken;
}

