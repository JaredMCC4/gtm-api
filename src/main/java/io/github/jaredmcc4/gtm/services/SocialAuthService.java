package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.dto.auth.JwtResponse;
import io.github.jaredmcc4.gtm.dto.auth.SocialLoginRequest;

/**
 * Contrato para autenticar usuarios mediante proveedores externos y emitir JWT propios.
 */
public interface SocialAuthService {

    /**
     * Intercambia el authorization code o access token contra el proveedor configurado, obtiene
     * el perfil del usuario y genera JWT + refresh token propios del sistema.
     *
     * @param request payload con proveedor y code/access token
     * @return respuesta con tokens listos para el frontend
     */
    JwtResponse login(SocialLoginRequest request);
}
