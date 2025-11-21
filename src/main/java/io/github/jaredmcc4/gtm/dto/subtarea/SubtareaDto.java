package io.github.jaredmcc4.gtm.dto.subtarea;

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
@Schema(description = "Subtarea perteneciente a una tarea.")
public class SubtareaDto {

    @Schema(description = "Identificador de la subtarea", example = "15")
    private Long id;

    @Schema(description = "Titulo de la subtarea", example = "Crear diagrama ERD")
    @NotBlank(message = "El titulo no puede estar vacio.")
    @Size(max = 120, message = "El titulo no puede exceder los {max} caracteres.")
    private String titulo;

    @Schema(description = "Estado de completado", example = "false")
    @Builder.Default
    private Boolean completada = false;
}
