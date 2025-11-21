package io.github.jaredmcc4.gtm.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload para actualizar datos basicos del usuario.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActualizarUsuarioRequest {
    @Size(max = 120)
    @Schema(description = "Nombre de usuario a mostrar", example = "Juan Perez")
    private String nombreUsuario;

    @Schema(description = "Zona horaria IANA", example = "America/Costa_Rica")
    private String zonaHoraria;
}

