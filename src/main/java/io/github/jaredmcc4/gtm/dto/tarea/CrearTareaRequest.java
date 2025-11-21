package io.github.jaredmcc4.gtm.dto.tarea;

import io.github.jaredmcc4.gtm.domain.Tarea;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Payload para crear una tarea nueva.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrearTareaRequest {
    @NotBlank
    @Size(min = 3, max = 120)
    @Schema(example = "Preparar demo")
    private String titulo;

    @Schema(example = "Incluir endpoint de reportes y métricas")
    private String descripcion;

    @Schema(description = "Prioridad de la tarea")
    private Tarea.Prioridad prioridad;

    @Schema(description = "Fecha de vencimiento en ISO", example = "2025-12-31T12:00:00")
    private LocalDateTime fechaVencimiento;

    @Schema(description = "IDs de etiquetas asociadas")
    private Set<Long> etiquetasIds;
}
