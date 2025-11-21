package io.github.jaredmcc4.gtm.dto.tarea;

import com.fasterxml.jackson.annotation.JsonFormat;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload para actualizar una tarea existente.")
public class ActualizarTareaRequest {

    @Schema(description = "Titulo de la tarea", example = "Implementar login")
    @NotBlank(message = "El titulo no puede estar vacio.")
    @Size(min = 3, max = 120, message = "El titulo debe tener entre {min} y {max} caracteres.")
    private String titulo;

    @Schema(description = "Descripcion opcional", example = "Incluir validaciones y mensajes de error")
    private String descripcion;

    @Schema(description = "Prioridad de la tarea", example = "ALTA")
    private Tarea.Prioridad prioridad;

    @Schema(description = "Estado de la tarea", example = "PENDIENTE")
    private Tarea.EstadoTarea estado;

    @Schema(description = "Fecha y hora de vencimiento", example = "2025-12-31T17:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaVencimiento;

    @Schema(description = "IDs de etiquetas asociadas", example = "[1,2]")
    private Set<Long> etiquetasIds;
}
