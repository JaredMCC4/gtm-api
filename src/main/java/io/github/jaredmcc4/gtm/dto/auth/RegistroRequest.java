package io.github.jaredmcc4.gtm.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload para registrar un nuevo usuario en el sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de registro de usuario.")
public class RegistroRequest {

    @Schema(description = "Email único del usuario", example = "nuevo@example.com")
    @NotBlank(message = "El email no puede estar vacio.")
    @Email(message = "Debe ser un email valido.")
    private String email;

    @Schema(description = "Contraseña en texto plano", example = "PasswordSegura1!")
    @NotBlank(message = "La contrasena no puede estar vacia.")
    @Size(min = 8, message = "La contrasena debe tener al menos {min} caracteres.")
    private String password;

    @Schema(description = "Nombre visible del usuario", example = "Usuario GTM")
    @Size(max = 120, message = "El nombre de usuario no puede exceder los {max} caracteres.")
    private String nombreUsuario;

    @Schema(description = "Zona horaria preferida", example = "America/Costa_Rica")
    private String zonaHoraria = "America/Costa_Rica";

    @Schema(description = "Token de Cloudflare Turnstile para verificación")
    @NotBlank(message = "El token de verificación es requerido.")
    private String turnstileToken;
}

