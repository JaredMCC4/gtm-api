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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

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

    @Override
    public Optional<RefreshToken> validarRefreshToken(String token) {
        return refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .filter(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void revocarRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    @Transactional
    public void limpiarRefreshTokensExpirados() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
