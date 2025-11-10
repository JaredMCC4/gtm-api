package io.github.jaredmcc4.gtm.dto.tarea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
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
public class TareaDto {

    private Long id;

    @NotBlank(message = "El título no puede estar vacío.")
    @Size(min = 3, max = 120, message = "El título debe tener entre {min} y {max} caracteres.")
    private String titulo;

    private String descripcion;

    private Tarea.Prioridad prioridad;

    private Tarea.EstadoTarea estado;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String fechaVencimiento;

    private Set<EtiquetaDto> etiquetas;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
