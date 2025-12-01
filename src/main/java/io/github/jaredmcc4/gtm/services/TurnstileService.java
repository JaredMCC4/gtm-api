package io.github.jaredmcc4.gtm.services;

/**
 * Servicio para verificar tokens de Cloudflare Turnstile.
 */
public interface TurnstileService {

    /**
     * Verifica un token de Turnstile con la API de Cloudflare.
     *
     * @param token el token del widget Turnstile
     * @param remoteIp la IP del cliente (opcional)
     * @return true si el token es válido
     * @throws IllegalArgumentException si el token es inválido o la verificación falla
     */
    boolean verificarToken(String token, String remoteIp);
}
