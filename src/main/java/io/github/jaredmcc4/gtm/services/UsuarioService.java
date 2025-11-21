package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Usuario;

/**
 * Contrato para operaciones sobre el perfil y credenciales del usuario.
 */
public interface UsuarioService {

    /**
     * Obtiene un usuario por su ID.
     *
     * @param usuarioId identificador interno
     * @return usuario encontrado
     */
    Usuario obtenerUsuarioPorId(Long usuarioId);

    /**
     * Actualiza datos basicos del usuario (nombre, zona horaria).
     *
     * @param usuarioId identificador del usuario
     * @param datosActualizados datos a modificar
     * @return usuario actualizado
     */
    Usuario actualizarUsuario(Long usuarioId, Usuario datosActualizados);

    /**
     * Cambia la contrasena validando la contrasena actual.
     *
     * @param usuarioId identificador del usuario
     * @param passwordActual contrasena vigente
     * @param passwordNueva nueva contrasena
     */
    void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva);
}
