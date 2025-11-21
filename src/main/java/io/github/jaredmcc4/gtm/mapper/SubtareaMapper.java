package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Subtarea;
import io.github.jaredmcc4.gtm.dto.subtarea.SubtareaDto;
import org.springframework.stereotype.Component;

@Component
public class SubtareaMapper {
    /**
     * Convierte una subtarea de dominio a su DTO.
     *
     * @param subtarea entidad origen (puede ser null)
     * @return DTO resultante o null
     */
    public SubtareaDto toDto(Subtarea subtarea) {
        if (subtarea == null){
            return null;
        }

        return SubtareaDto.builder()
                .id(subtarea.getId())
                .titulo(subtarea.getTitulo())
                .completada(Boolean.TRUE.equals(subtarea.getCompletada()))
                .build();
    }

    /**
     * Convierte un DTO de subtarea a entidad de dominio.
     *
     * @param subtareaDto dto origen (puede ser null)
     * @return entidad resultante o null
     */
    public Subtarea toEntity(SubtareaDto subtareaDto){
        if (subtareaDto == null) {
            return null;
        }

        return Subtarea.builder()
                .id(subtareaDto.getId())
                .titulo(subtareaDto.getTitulo())
                .completada(subtareaDto.getCompletada() != null ? subtareaDto.getCompletada() : false)
                .build();
    }

}
