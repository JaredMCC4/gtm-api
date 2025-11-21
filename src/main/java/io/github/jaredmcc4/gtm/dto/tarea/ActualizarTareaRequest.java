package io.github.jaredmcc4.gtm.dto.tarea;

import io.github.jaredmcc4.gtm.domain.Tarea;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Payload para actualizar una tarea existente.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActualizarTareaRequest {
    @Size(min = 3, max = 120)
    @Schema(example = "Actualizar dashboard de métricas")
    private String titulo;

    @Schema(example = "Agregar grafico de latencia por endpoint")
    private String descripcion;

    @Schema(description = "Prioridad de la tarea")
    private Tarea.Prioridad prioridad;

    @Schema(description = "Estado de la tarea")
    private Tarea.EstadoTarea estado;

    @Schema(description = "Fecha de vencimiento en ISO", example = "2025-12-31T12:00:00")
    private LocalDateTime fechaVencimiento;

    @Schema(description = "IDs de etiquetas asociadas")
    private Set<Long> etiquetasIds;
}

