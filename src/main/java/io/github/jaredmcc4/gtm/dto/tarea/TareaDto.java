package io.github.jaredmcc4.gtm.dto.tarea;

import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO de salida para tareas, usado en las respuestas de la API.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TareaDto {
    @Schema(example = "1")
    private Long id;

    @Schema(description = "Titulo de la tarea", example = "Realizar backup semanal")
    private String titulo;

    @Schema(description = "Descripcion detallada", example = "Hacer backup incremental de la base de datos")
    private String descripcion;

    @Schema(description = "Prioridad de la tarea")
    private Tarea.Prioridad prioridad;

    @Schema(description = "Estado actual de la tarea")
    private Tarea.EstadoTarea estado;

    @Schema(description = "Fecha de vencimiento en formato ISO", example = "2025-12-31T23:59:59")
    private String fechaVencimiento;

    @Schema(description = "Etiquetas asociadas")
    private Set<EtiquetaDto> etiquetas;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

