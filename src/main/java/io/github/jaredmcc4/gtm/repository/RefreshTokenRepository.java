package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    public Optional<RefreshToken> findByToken(String token);
    public Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    public void deleteExpiredTokens(LocalDateTime now);

    public void deleteByUsuarioId(Long usuarioId);
}
