package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.RefreshToken;
import io.github.jaredmcc4.gtm.domain.Usuario;

import java.util.Optional;

public interface RefreshTokenService {

    public RefreshToken crearRefreshToken(Usuario usuario, long validezTiempo);

    public Optional<RefreshToken> validarRefreshToken(String token);

    public void revocarRefreshToken(String token);
    public void limpiarRefreshTokensExpirados();

}
