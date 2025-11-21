package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Rol;

import java.util.List;

/**
 * Contrato para administrar roles de aplicacion y asignarlos a usuarios.
 */
public interface RolService {

    /**
     * Crea un rol en el sistema.
     *
     * @param rol entidad de rol con nombre unico
     * @return rol creado
     */
    Rol crearRol(Rol rol);

    /**
     * Lista todos los roles disponibles.
     *
     * @return roles disponibles
     */
    List<Rol> mostrarRoles();

    /**
     * Asigna un rol existente a un usuario.
     *
     * @param usuarioId identificador del usuario
     * @param nombreRol nombre del rol a asignar
     */
    void asignarRol(Long usuarioId, String nombreRol);
}
