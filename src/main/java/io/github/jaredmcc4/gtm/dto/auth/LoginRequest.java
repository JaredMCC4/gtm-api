package io.github.jaredmcc4.gtm.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload para solicitar autenticacion mediante email y contraseña.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud de inicio de sesión con email y contraseña.")
public class LoginRequest {

    @Schema(description = "Email del usuario", example = "admin@example.com")
    @NotBlank(message = "El email no puede estar vacio.")
    @Email(message = "Debe introducir un email valido.")
    private String email;

    @Schema(description = "Contraseña en texto plano", example = "password123.")
    @NotBlank(message = "La contrasena no puede estar vacia.")
    private String password;
}

