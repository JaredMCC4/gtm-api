package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Rol;

import java.util.List;

public interface RolService {

    public Rol crearRol(Rol rol);

    public List<Rol> mostrarRoles();

    public void asignarRol(Long usuarioId, String nombreRol);
}
