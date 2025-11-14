package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
import org.springframework.stereotype.Component;

@Component
public class EtiquetaMapper {

    public EtiquetaDto toDto(Etiqueta etiqueta) {
        if (etiqueta == null){
            return null;
        }

        return EtiquetaDto.builder()
                .id(etiqueta.getId())
                .nombre(etiqueta.getNombre())
                .colorHex(etiqueta.getColorHex())
                .build();
    }

    public Etiqueta toEntity(EtiquetaDto etiquetaDto) {
        if (etiquetaDto == null){
            return null;
        }

        return Etiqueta.builder()
                .id(etiquetaDto.getId())
                .nombre(etiquetaDto.getNombre())
                .colorHex(etiquetaDto.getColorHex())
                .build();
    }
}
