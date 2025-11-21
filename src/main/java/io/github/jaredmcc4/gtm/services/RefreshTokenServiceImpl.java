package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.RefreshToken;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementacion de {@link RefreshTokenService} con persistencia en base de datos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Crea y guarda un refresh token para el usuario con la vigencia indicada.
     */
    @Override
    @Transactional
    public RefreshToken crearRefreshToken(Usuario usuario, long validezTiempo) {
        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusMinutes(validezTiempo))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Valida que el token exista, no este revocado y no haya expirado.
     */
    @Override
    public Optional<RefreshToken> validarRefreshToken(String token) {
        return refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .filter(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    /**
     * Marca como revocado un refresh token especifico.
     */
    @Override
    @Transactional
    public void revocarRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    /**
     * Elimina los tokens cuya fecha de expiracion ya paso.
     */
    @Override
    @Transactional
    public void limpiarRefreshTokensExpirados() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}

