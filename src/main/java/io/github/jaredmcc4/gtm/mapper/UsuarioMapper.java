package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.usuario.UsuarioDto;
import io.github.jaredmcc4.gtm.dto.rol.RolDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UsuarioMapper {

    /**
     * Convierte una entidad Usuario en su representacion DTO incluyendo roles y estado.
     *
     * @param usuario entidad origen (puede ser null)
     * @return DTO con datos visibles o null si la entrada es null
     */
    public UsuarioDto toDto(Usuario usuario){
        if (usuario == null){
            return null;
        }

        return UsuarioDto.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombreUsuario(usuario.getNombreUsuario())
                .zonaHoraria(usuario.getZonaHoraria())
                .activo(usuario.isActivo())
                .roles(usuario.getRoles().stream()
                        .map(rol -> RolDto.builder()
                                .id(rol.getId())
                                .nombreRol(rol.getNombreRol())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }
}
