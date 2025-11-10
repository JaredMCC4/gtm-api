package io.github.jaredmcc4.gtm.dto.etiqueta;

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
public class EtiquetaDto {

    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío.")
    @Size(max = 60, message = "El nombre no puede exceder los {max} caracteres.")
    private String nombre;

    @NotBlank(message = "El color no puede estar vacío.")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe seguir el formato hexadecimal (#RRGGBB).")
    private String colorHex;
}
