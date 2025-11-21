package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.usuario.UsuarioDto;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UsuarioMapper - Unit Tests")
class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapper();

    @Test
    @DisplayName("Debe mapear usuario a DTO")
    void deberiaMapearUsuario() {
        Usuario usuario = Usuario.builder()
                .id(5L)
                .email("usuario@test.com")
                .nombreUsuario("Usuario Test")
                .zonaHoraria("UTC")
                .activo(true)
                .roles(Set.of(Rol.builder().id(1L).nombreRol("ADMIN").build()))
                .build();

        UsuarioDto dto = mapper.toDto(usuario);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getEmail()).isEqualTo("usuario@test.com");
        assertThat(dto.isActivo()).isTrue();
        assertThat(dto.getRoles())
                .hasSize(1)
                .extracting("nombreRol")
                .containsExactly("ADMIN");
    }

    @Test
    @DisplayName("Debe devolver null si el usuario es null")
    void deberiaRetornarNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
