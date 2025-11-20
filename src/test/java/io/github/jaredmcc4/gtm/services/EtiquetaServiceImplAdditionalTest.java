package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.builders.EtiquetaTestBuilder;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.repository.EtiquetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EtiquetaServiceImpl - Cobertura adicional")
class EtiquetaServiceImplAdditionalTest {

    @Mock
    private EtiquetaRepository etiquetaRepository;

    @InjectMocks
    private EtiquetaServiceImpl etiquetaService;

    private Usuario usuario;
    private Etiqueta etiqueta;

    @BeforeEach
    void setUp() {
        usuario = UsuarioTestBuilder.unUsuario().conId(1L).build();
        etiqueta = EtiquetaTestBuilder.unaEtiqueta()
                .conId(10L)
                .conNombre("Trabajo")
                .conColor("#FF0000")
                .conUsuario(usuario)
                .build();
    }

    @Test
    @DisplayName("crearEtiqueta debe rechazar nombres null")
    void deberiaRechazarNombreNull() {
        Etiqueta invalida = EtiquetaTestBuilder.unaEtiqueta()
                .conNombre(null)
                .conColor("#FFFFFF")
                .build();

        assertThatThrownBy(() -> etiquetaService.crearEtiqueta(invalida, usuario))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("crearEtiqueta debe rechazar color null")
    void deberiaRechazarColorNull() {
        Etiqueta invalida = EtiquetaTestBuilder.unaEtiqueta()
                .conColor(null)
                .build();

        assertThatThrownBy(() -> etiquetaService.crearEtiqueta(invalida, usuario))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("actualizarEtiqueta no debe validar duplicado si el nombre es igual")
    void deberiaActualizarSinVerificarDuplicadoCuandoNombreIgual() {
        when(etiquetaRepository.findById(10L)).thenReturn(Optional.of(etiqueta));
        when(etiquetaRepository.save(any(Etiqueta.class))).thenAnswer(inv -> inv.getArgument(0));

        etiquetaService.actualizarEtiqueta(10L, etiqueta, 1L);

        verify(etiquetaRepository, never()).existsByUsuarioIdAndNombre(anyLong(), any());
    }
}
