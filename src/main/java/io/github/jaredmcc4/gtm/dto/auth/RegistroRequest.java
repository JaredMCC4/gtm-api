package io.github.jaredmcc4.gtm.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequest {
    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "Debe ser un email válido.")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía.")
    @Size(min = 8, message = "La contraseña debe tener al menos {min} caracteres.")
    private String password;

    @Size(max = 120, message = "El nombre de usuario no puede exceder los {max} caracteres.")
    private String nombreUsuario;

    private String zonaHoraria = "America/Costa_Rica";
}
