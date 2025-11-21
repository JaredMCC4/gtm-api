package io.github.jaredmcc4.gtm.dto.rol;

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
@Schema(description = "Rol disponible para un usuario (USER, ADMIN, etc.)")
public class RolDto {

    @Schema(description = "Identificador del rol", example = "1")
    private Long id;

    @Schema(description = "Nombre del rol", example = "ADMIN")
    @NotBlank(message = "El nombre del rol no puede estar vacio.")
    @Size(max = 50, message = "El nombre del rol no puede exceder los {max} caracteres.")
    private String nombreRol;
}
