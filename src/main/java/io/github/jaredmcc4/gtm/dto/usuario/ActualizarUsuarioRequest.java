package io.github.jaredmcc4.gtm.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload para actualizar el perfil del usuario.")
public class ActualizarUsuarioRequest {

    @Schema(description = "Nombre visible del usuario", example = "Jared Ch")
    @Size(max = 120, message = "El nombre no puede exceder los {max} caracteres.")
    private String nombreUsuario;

    @Schema(description = "Zona horaria preferida", example = "America/Costa_Rica")
    private String zonaHoraria;
}
