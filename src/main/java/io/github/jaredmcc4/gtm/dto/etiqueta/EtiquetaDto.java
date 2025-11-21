package io.github.jaredmcc4.gtm.dto.etiqueta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear/actualizar/mostrar etiquetas del usuario.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EtiquetaDto {
    @Schema(example = "1")
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Schema(description = "Nombre de la etiqueta", example = "Urgente")
    private String nombre;

    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    @Schema(description = "Color en formato hexadecimal", example = "#FF0000")
    private String colorHex;
}

