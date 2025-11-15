package io.github.jaredmcc4.gtm.builders;

import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Usuario;

import java.util.HashSet;
import java.util.Set;

public class UsuarioTestBuilder {

    private Long id = 1L;
    private String email = "test@test.com";
    private String contrasenaHash = "";
    private String nombreUsuario = "Test_User";
    private String zonaHoraria = "America/Costa_Rica";
    private Boolean activo = true;
    private Set<Rol> roles = new HashSet<>();

    public static UsuarioTestBuilder unUsuario() {
        return new UsuarioTestBuilder();
    }

    public UsuarioTestBuilder conId(Long id) {
        this.id = id;
        return this;
    }

    public UsuarioTestBuilder conEmail(String email) {
        this.email = email;
        return this;
    }

    public UsuarioTestBuilder conContrasenaHash(String hash) {
        this.contrasenaHash = hash;
        return this;
    }

    public UsuarioTestBuilder conNombreUsuario(String nombre) {
        this.nombreUsuario = nombre;
        return this;
    }

    public UsuarioTestBuilder inactivo() {
        this.activo = false;
        return this;
    }

    public UsuarioTestBuilder conRol(Rol rol) {
        this.roles.add(rol);
        return this;
    }

    public Usuario build() {
        Usuario usuario = Usuario.builder()
                .id(id)
                .email(email)
                .contrasenaHash(contrasenaHash)
                .nombreUsuario(nombreUsuario)
                .zonaHoraria(zonaHoraria)
                .activo(activo)
                .roles(new HashSet<>(roles))
                .build();
        return usuario;
    }
}
