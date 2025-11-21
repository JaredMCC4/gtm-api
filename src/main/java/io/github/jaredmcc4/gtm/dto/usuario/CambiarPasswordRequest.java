package io.github.jaredmcc4.gtm.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para cambiar la contrasena del usuario.")
public class CambiarPasswordRequest {

    @Schema(description = "Contrasena actual", example = "PasswordActual1!")
    @NotBlank(message = "La contrasena actual no puede estar vacia.")
    private String contrasenaActual;

    @Schema(description = "Nueva contrasena", example = "PasswordNueva2!")
    @NotBlank(message = "La nueva contrasena no puede estar vacia.")
    @Size(min = 8, message = "La contrasena debe tener al menos {min} caracteres.")
    private String nuevaContrasena;
}
