package io.github.jaredmcc4.gtm.dto.rol;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un rol asignable a usuarios.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RolDto {
    @Schema(example = "1")
    private Long id;

    @Schema(description = "Nombre del rol", example = "USER")
    private String nombreRol;
}

