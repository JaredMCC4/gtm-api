package io.github.jaredmcc4.gtm.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload para solicitar cambio de contraseña.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CambiarPasswordRequest {
    @NotBlank
    @Schema(description = "Contraseña actual del usuario", example = "OldP@ssw0rd")
    private String contrasenaActual;

    @NotBlank
    @Size(min = 8)
    @Schema(description = "Nueva contraseña (min 8 caracteres)", example = "Nuev0P@ss!")
    private String nuevaContrasena;
}

