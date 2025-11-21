package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
import org.springframework.stereotype.Component;

@Component
public class EtiquetaMapper {

    /**
     * Convierte una entidad de etiqueta al DTO de exposicion.
     *
     * @param etiqueta entidad origen (puede ser null)
     * @return DTO resultante o null
     */
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

    /**
     * Convierte un DTO de etiqueta en entidad de dominio.
     *
     * @param etiquetaDto dto origen (puede ser null)
     * @return entidad resultante o null
     */
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
