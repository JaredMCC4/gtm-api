package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Acceso a tokens de refresco persistidos, permitiendo busquedas y limpiezas programadas.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un token especifico por su valor (puede estar revocado).
     *
     * @param token token JWT de refresco en texto plano
     * @return token encontrado o vacio
     */
    public Optional<RefreshToken> findByToken(String token);

    /**
     * Busca un token solo si no ha sido revocado.
     *
     * @param token token JWT de refresco en texto plano
     * @return token activo o vacio
     */
    public Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    /**
     * Elimina tokens expirados (cron o job programado).
     *
     * @param now instante limite a considerar vencido
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    public void deleteExpiredTokens(LocalDateTime now);

    /**
     * Borra todos los refresh tokens asociados a un usuario.
     *
     * @param usuarioId identificador del usuario
     */
    public void deleteByUsuarioId(Long usuarioId);
}
