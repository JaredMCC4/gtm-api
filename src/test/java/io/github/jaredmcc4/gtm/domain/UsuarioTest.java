package io.github.jaredmcc4.gtm.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuario - Builder y colecciones")
class UsuarioTest {

    @Test
    @DisplayName("El builder debe copiar el set de roles y no compartir referencia")
    void builderDebeCopiarRoles() {
        Rol rol = Rol.builder().id(1L).nombreRol("USER").build();
        Set<Rol> rolesOriginales = new HashSet<>();
        rolesOriginales.add(rol);

        Usuario usuario = Usuario.builder()
                .email("test@example.com")
                .contrasenaHash("hash")
                .roles(rolesOriginales)
                .build();

        rolesOriginales.clear();

        assertThat(usuario.getRoles())
                .hasSize(1)
                .allMatch(r -> r.getNombreRol().equals("USER"));
    }

    @Test
    @DisplayName("El builder debe asignar un set vacío cuando se envía null")
    void builderDebeAsignarSetVacioCuandoEsNull() {
        Usuario usuario = Usuario.builder()
                .email("test@example.com")
                .contrasenaHash("hash")
                .roles(null)
                .build();

        assertThat(usuario.getRoles())
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("setRoles debe clonar el conjunto recibido")
    void setRolesDebeClonarConjunto() {
        Usuario usuario = Usuario.builder()
                .email("test@example.com")
                .contrasenaHash("hash")
                .build();

        Set<Rol> roles = new HashSet<>();
        roles.add(Rol.builder().id(2L).nombreRol("ADMIN").build());

        usuario.setRoles(roles);
        roles.clear();

        assertThat(usuario.getRoles())
                .hasSize(1)
                .allMatch(r -> r.getNombreRol().equals("ADMIN"));
    }
}
