package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Subtarea;
import io.github.jaredmcc4.gtm.dto.subtarea.SubtareaDto;
import org.springframework.stereotype.Component;

@Component
public class SubtareaMapper {
    public SubtareaDto toDto(Subtarea subtarea) {
        if (subtarea == null){
            return null;
        }

        return SubtareaDto.builder()
                .id(subtarea.getId())
                .titulo(subtarea.getTitulo())
                .completada(subtarea.getCompletada())
                .build();
    }

    public Subtarea toEntity(SubtareaDto subtareaDto){
        if (subtareaDto == null) {
            return null;
        }

        return Subtarea.builder()
                .id(subtareaDto.getId())
                .titulo(subtareaDto.getTitulo())
                .completada(subtareaDto.getCompletada())
                .build();
    }

}
