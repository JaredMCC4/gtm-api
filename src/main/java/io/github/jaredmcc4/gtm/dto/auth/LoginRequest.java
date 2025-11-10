package io.github.jaredmcc4.gtm.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "Debe introducir un email válido.")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía.")
    private String password;
}
