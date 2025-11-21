package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.dto.rol.RolDto;
import org.springframework.stereotype.Component;

@Component
public class RolMapper {
    /**
     * Convierte una entidad de rol a DTO.
     *
     * @param rol entidad origen (puede ser null)
     * @return DTO resultante o null
     */
    public RolDto toDto(Rol rol) {
        if (rol == null){
            return null;
        }

        return RolDto.builder()
                .id(rol.getId())
                .nombreRol(rol.getNombreRol())
                .build();
    }

    /**
     * Convierte un DTO de rol a entidad.
     *
     * @param rolDto dto origen (puede ser null)
     * @return entidad resultante o null
     */
    public Rol toEntity(RolDto rolDto) {
        if (rolDto == null) {
            return null;
        }

        return Rol.builder()
                .id(rolDto.getId())
                .nombreRol(rolDto.getNombreRol())
                .build();
    }
}
