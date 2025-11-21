package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.dto.tarea.TareaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TareaMapper {
    private final EtiquetaMapper etiquetaMapper;

    /**
     * Convierte una entidad {@link Tarea} en su DTO con etiquetas asociadas.
     *
     * @param tarea entidad origen (puede ser null)
     * @return DTO resultante o null si entrada es null
     */
    public TareaDto toDto(Tarea tarea){
        if (tarea == null){
            return null;
        }

        return TareaDto.builder()
                .id(tarea.getId())
                .titulo(tarea.getTitulo())
                .descripcion(tarea.getDescripcion())
                .prioridad(tarea.getPrioridad())
                .estado(tarea.getEstado())
                .fechaVencimiento(tarea.getFechaVencimiento() != null ? tarea.getFechaVencimiento().toString() : null)
                .etiquetas(tarea.getEtiquetas().stream()
                        .map(etiquetaMapper::toDto)
                        .collect(Collectors.toSet()))
                .createdAt(tarea.getCreatedAt())
                .updatedAt(tarea.getUpdatedAt())
                .build();
    }
}
