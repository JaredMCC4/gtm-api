package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.auth.JwtResponse;
import io.github.jaredmcc4.gtm.dto.auth.LoginRequest;
import io.github.jaredmcc4.gtm.dto.auth.RegistroRequest;

/**
 * Contrato de autenticacion y emision/gestion de tokens JWT + refresh.
 */
public interface AuthService {

    /**
     * Crea un nuevo usuario con los datos suministrados.
     *
     * @param registroRequest datos de registro
     * @return usuario persistido
     */
    Usuario registrarUsuario(RegistroRequest registroRequest);

    /**
     * Autentica credenciales y genera JWT + refresh token.
     *
     * @param loginRequest email y password
     * @return respuesta con tokens y expiraciones
     */
    JwtResponse autenticarUsuario(LoginRequest loginRequest);

    /**
     * Genera un nuevo par de tokens a partir de un refresh valido.
     *
     * @param refreshToken refresh token en vigor
     * @return nuevos tokens
     */
    JwtResponse refrescarToken(String refreshToken);

    /**
     * Revoca un refresh token y cierra la sesion.
     *
     * @param refreshToken refresh token a revocar
     */
    void cerrarSesion(String refreshToken);

    /**
     * Verifica integridad y vigencia de un JWT de acceso.
     *
     * @param token JWT a validar
     */
    void validarToken(String token);

    /**
     * Emite un JWT y refresh token para un usuario ya validado (por ejemplo, login social).
     *
     * @param usuario entidad autenticada
     * @return respuesta con tokens listos para el frontend
     */
    JwtResponse emitirTokensParaUsuario(Usuario usuario);
}
