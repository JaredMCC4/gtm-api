package io.github.jaredmcc4.gtm.config;

import io.github.jaredmcc4.gtm.repository.AdjuntoRepository;
import io.github.jaredmcc4.gtm.repository.EtiquetaRepository;
import io.github.jaredmcc4.gtm.repository.RefreshTokenRepository;
import io.github.jaredmcc4.gtm.repository.RolRepository;
import io.github.jaredmcc4.gtm.repository.SubtareaRepository;
import io.github.jaredmcc4.gtm.repository.TareaRepository;
import io.github.jaredmcc4.gtm.repository.UsuarioRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockRepositoriesConfig {

    @Bean
    public UsuarioRepository usuarioRepository() {
        return Mockito.mock(UsuarioRepository.class);
    }

    @Bean
    public RolRepository rolRepository() {
        return Mockito.mock(RolRepository.class);
    }

    @Bean
    public RefreshTokenRepository refreshTokenRepository() {
        return Mockito.mock(RefreshTokenRepository.class);
    }

    @Bean
    public TareaRepository tareaRepository() {
        return Mockito.mock(TareaRepository.class);
    }

    @Bean
    public EtiquetaRepository etiquetaRepository() {
        return Mockito.mock(EtiquetaRepository.class);
    }

    @Bean
    public SubtareaRepository subtareaRepository() {
        return Mockito.mock(SubtareaRepository.class);
    }

    @Bean
    public AdjuntoRepository adjuntoRepository() {
        return Mockito.mock(AdjuntoRepository.class);
    }
}
