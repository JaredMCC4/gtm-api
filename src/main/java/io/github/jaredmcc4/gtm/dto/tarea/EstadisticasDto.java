package io.github.jaredmcc4.gtm.dto.tarea;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que resume conteos de tareas por estado para un usuario.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasDto {
    @Schema(description = "Cantidad de tareas pendientes", example = "12")
    private long pendientes;
    @Schema(description = "Cantidad de tareas completadas", example = "30")
    private long completadas;
    @Schema(description = "Cantidad de tareas canceladas", example = "5")
    private long canceladas;
    @Schema(description = "Total de tareas", example = "47")
    private long total;
}

