package io.github.jaredmcc4.gtm.dto.etiqueta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Etiqueta asociable a las tareas de un usuario.")
public class EtiquetaDto {

    @Schema(description = "Identificador de la etiqueta", example = "3")
    private Long id;

    @Schema(description = "Nombre visible de la etiqueta", example = "Backend")
    @NotBlank(message = "El nombre no puede estar vacio.")
    @Size(max = 60, message = "El nombre no puede exceder los {max} caracteres.")
    private String nombre;

    @Schema(description = "Color en formato hexadecimal #RRGGBB", example = "#FF6600")
    @NotBlank(message = "El color no puede estar vacio.")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe seguir el formato hexadecimal (#RRGGBB).")
    private String colorHex;
}
