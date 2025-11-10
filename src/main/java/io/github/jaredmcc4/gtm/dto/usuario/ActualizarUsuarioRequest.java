package io.github.jaredmcc4.gtm.dto.usuario;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActualizarUsuarioRequest {

    @Size(max = 120, message = "El nombre de usuario no puede exceder los {max} caracteres.")
    private String nombreUsuario;

    private String zonaHoraria;
}
