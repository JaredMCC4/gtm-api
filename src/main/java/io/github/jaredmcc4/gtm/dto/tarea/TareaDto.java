package io.github.jaredmcc4.gtm.dto.tarea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Representacion de una tarea del usuario.")
public class TareaDto {

    @Schema(description = "Identificador de la tarea", example = "25")
    private Long id;

    @Schema(description = "Titulo de la tarea", example = "Implementar login")
    private String titulo;

    @Schema(description = "Descripcion opcional", example = "Incluir validaciones y mensajes de error")
    private String descripcion;

    @Schema(description = "Prioridad asignada", example = "ALTA")
    private Tarea.Prioridad prioridad;

    @Schema(description = "Estado actual", example = "PENDIENTE")
    private Tarea.EstadoTarea estado;

    @Schema(description = "Fecha y hora de vencimiento en ISO-8601", example = "2025-12-31T17:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String fechaVencimiento;

    @Schema(description = "Etiquetas asociadas")
    private Set<EtiquetaDto> etiquetas;

    @Schema(description = "Fecha de creacion", example = "2025-11-20T12:34:56")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de ultima actualizacion", example = "2025-11-21T09:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
