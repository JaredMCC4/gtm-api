package io.github.jaredmcc4.gtm.dto.tarea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Conteo de tareas por estado.")
public class EstadisticasDto {
    @Schema(description = "Total de tareas pendientes.", example = "15")
    private long pendientes;
    @Schema(description = "Total de tareas completadas.", example = "25")
    private long completadas;
    @Schema(description = "Total de tareas canceladas.", example = "5")
    private long canceladas;
    @Schema(description = "Total de tareas del usuario.", example = "45")
    private long total;
}
