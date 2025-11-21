package io.github.jaredmcc4.gtm.dto.subtarea;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para subtareas en respuestas y peticiones.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubtareaDto {
    @Schema(example = "1")
    private Long id;

    @Schema(description = "Titulo de la subtarea", example = "Crear script de backup incremental")
    @NotBlank(message = "El titulo de la subtarea no puede estar vacio")
    @Size(min = 1, max = 120, message = "El titulo debe tener entre 1 y 120 caracteres")
    private String titulo;

    @Schema(description = "Estado de completado", example = "false")
    private Boolean completada;
}
