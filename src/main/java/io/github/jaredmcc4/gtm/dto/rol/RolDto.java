package io.github.jaredmcc4.gtm.dto.rol;

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
public class RolDto {

    private Long id;

    @NotBlank(message = "El nombre del rol no puede estar vacío.")
    @Size(max = 50, message = "El nombre del rol no puede exceder los {max} caracteres.")
    private String nombreRol;
}
