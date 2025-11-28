package io.github.jaredmcc4.gtm.dto.auth;

/**
 * Datos mínimos obtenidos desde el proveedor externo.
 *
 * @param email       email verificado o principal
 * @param displayName nombre visible si está disponible
 * @param providerId  identificador único en el proveedor
 */
public record OAuthUserInfo(String email, String displayName, String providerId) {
}
