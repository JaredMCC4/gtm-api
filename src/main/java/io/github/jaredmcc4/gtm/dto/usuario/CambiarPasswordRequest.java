package io.github.jaredmcc4.gtm.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarPasswordRequest {

    @NotBlank(message = "La contraseña no puede estar vacía.")
    private String passwordActual;

    @NotBlank(message = "La contraseña nueva no puede estar vacía.")
    @Size(min = 8, message = "La contraseña debe tener al menos {min} caracteres.")
    private String passwordNueva;
}
