package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Usuario;

public interface UsuarioService {

    public Usuario obtenerUsuarioPorId(Long usuarioId);
    public Usuario actualizarUsuario(Long usuarioId, Usuario datosActualizados);

    public void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva);
}
