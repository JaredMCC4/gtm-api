package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.RefreshToken;
import io.github.jaredmcc4.gtm.domain.Usuario;

import java.util.Optional;

/**
 * Contrato para gestionar tokens de refresco persistidos.
 */
public interface RefreshTokenService {

    /**
     * Crea y persiste un refresh token para el usuario indicado.
     *
     * @param usuario usuario propietario
     * @param validezTiempo tiempo de vigencia en milisegundos
     * @return token generado
     */
    RefreshToken crearRefreshToken(Usuario usuario, long validezTiempo);

    /**
     * Valida formato, vigencia y estado de un refresh token.
     *
     * @param token cadena del refresh token
     * @return token opcional si es valido y no revocado
     */
    Optional<RefreshToken> validarRefreshToken(String token);

    /**
     * Revoca un refresh token especifico.
     *
     * @param token cadena del refresh token
     */
    void revocarRefreshToken(String token);

    /**
     * Elimina o marca como revocados los refresh tokens ya expirados.
     */
    void limpiarRefreshTokensExpirados();

}
