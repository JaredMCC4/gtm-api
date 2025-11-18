package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.dto.rol.RolDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("Rol Mapper - Unit Tests")
class RolMapperTest {

    @Autowired
    private RolMapper rolMapper;

    @Test
    @DisplayName("Debería mapear Rol a RolDto correctamente")
    void deberiaMapeARolDto() {

        Rol rol = Rol.builder()
                .id(1L)
                .nombreRol("USER")
                .build();

        RolDto resultado = rolMapper.toDto(rol);
        
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombreRol()).isEqualTo("USER");
    }

    @Test
    @DisplayName("Debería retornar null si Rol es null")
    void deberiaRetornarNullSiRolEsNull() {

        RolDto resultado = rolMapper.toDto(null);
        assertThat(resultado).isNull();
    }
}