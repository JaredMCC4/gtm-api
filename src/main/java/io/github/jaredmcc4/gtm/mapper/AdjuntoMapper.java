package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.dto.adjunto.AdjuntoDto;
import org.springframework.stereotype.Component;

@Component
public class AdjuntoMapper {

    /**
     * Convierte un adjunto de dominio a DTO exponiendo metadatos del archivo.
     *
     * @param adjunto entidad origen (puede ser null)
     * @return DTO resultante o null
     */
    public AdjuntoDto toDto(Adjunto adjunto) {
        if (adjunto == null){
            return null;
        }
        return AdjuntoDto.builder()
                .id(adjunto.getId())
                .nombre(adjunto.getNombre())
                .mimeType(adjunto.getMimeType())
                .sizeBytes(adjunto.getSizeBytes())
                .path(adjunto.getPath())
                .updatedAt(adjunto.getUploadedAt())
                .build();
    }
}
