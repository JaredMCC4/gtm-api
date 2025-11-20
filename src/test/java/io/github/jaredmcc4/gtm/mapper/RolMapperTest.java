package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.dto.rol.RolDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RolMapper - Unit Tests")
class RolMapperTest {

    private final RolMapper rolMapper = new RolMapper();

    @Test
    @DisplayName("Debe convertir DTO a entidad correctamente")
    void deberiaConvertirDtoEnEntity() {
        RolDto dto = RolDto.builder()
                .id(5L)
                .nombreRol("ADMIN")
                .build();

        Rol resultado = rolMapper.toEntity(dto);

        assertThat(resultado.getId()).isEqualTo(5L);
        assertThat(resultado.getNombreRol()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Debe retornar null al convertir DTO nulo")
    void deberiaRetornarNullConDtoNulo() {
        assertThat(rolMapper.toEntity(null)).isNull();
    }
}
