package io.github.jaredmcc4.gtm.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuario - Builder y utilidades")
class UsuarioTest {

    @Test
    @DisplayName("El builder debe copiar defensivamente los roles")
    void builderDebeCopiarRoles() {
        Set<Rol> roles = new HashSet<>();
        roles.add(Rol.builder().id(1L).nombreRol("USER").build());

        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("user@test.com")
                .roles(roles)
                .build();

        roles.add(Rol.builder().id(2L).nombreRol("ADMIN").build());

        assertThat(usuario.getRoles()).hasSize(1);
    }

    @Test
    @DisplayName("setRoles debe aceptar valores null y crear un set vacío")
    void setRolesDebeAceptarNull() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("user@test.com")
                .build();

        usuario.setRoles(null);

        assertThat(usuario.getRoles()).isNotNull();
        assertThat(usuario.getRoles()).isEmpty();
    }
}
