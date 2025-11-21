package io.github.jaredmcc4.gtm.dto.subtarea;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private String titulo;

    @Schema(description = "Estado de completado", example = "false")
    private Boolean completada;
}

