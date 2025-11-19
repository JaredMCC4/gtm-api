package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.builders.EtiquetaTestBuilder;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.DuplicateResourceException;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.repository.EtiquetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Etiqueta Service - Unit Tests")
class EtiquetaServiceImplTest {

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
                .conId(1L)
                .conNombre("Trabajo")
                .conColor("#FF5733")
                .conUsuario(usuario)
                .build();
    }

    @Nested
    @DisplayName("crearEtiqueta()")
    class CrearEtiquetaTests {

        @Test
        @DisplayName("Debería crear etiqueta correctamente")
        void deberiaCrearEtiqueta() {
            Etiqueta nuevaEtiqueta = EtiquetaTestBuilder.unaEtiqueta()
                    .conNombre("Personal")
                    .conColor("#00FF00")
                    .build();

            when(etiquetaRepository.existsByUsuarioIdAndNombre(1L, "Personal")).thenReturn(false);
            when(etiquetaRepository.save(any(Etiqueta.class)))
                    .thenAnswer(inv -> {
                        Etiqueta e = inv.getArgument(0);
                        e.setId(2L);
                        return e;
                    });

            Etiqueta resultado = etiquetaService.crearEtiqueta(nuevaEtiqueta, usuario);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNombre()).isEqualTo("Personal");
            assertThat(resultado.getColorHex()).isEqualTo("#00FF00");
            assertThat(resultado.getUsuario()).isEqualTo(usuario);

            verify(etiquetaRepository).save(any(Etiqueta.class));
        }

        @Test
        @DisplayName("Debería rechazar nombre duplicado")
        void deberiaRechazarNombreDuplicado() {
            Etiqueta nuevaEtiqueta = EtiquetaTestBuilder.unaEtiqueta()
                    .conNombre("Trabajo")
                    .conColor("#FF0000")
                    .build();

            when(etiquetaRepository.existsByUsuarioIdAndNombre(1L, "Trabajo")).thenReturn(true);

            assertThatThrownBy(() -> etiquetaService.crearEtiqueta(nuevaEtiqueta, usuario))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Ya existe una etiqueta con el nombre: " + nuevaEtiqueta.getNombre());

            verify(etiquetaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería validar formato de color hexadecimal")
        void deberiaValidarFormatoColor() {
            Etiqueta etiquetaInvalida = EtiquetaTestBuilder.unaEtiqueta()
                    .conNombre("Test")
                    .conColor("rojo")
                    .build();

            when(etiquetaRepository.existsByUsuarioIdAndNombre(1L, "Test")).thenReturn(false);

            assertThatThrownBy(() -> etiquetaService.crearEtiqueta(etiquetaInvalida, usuario))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("hexadecimal");

            verify(etiquetaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar nombre vacío")
        void deberiaRechazarNombreVacio() {
            Etiqueta etiquetaInvalida = EtiquetaTestBuilder.unaEtiqueta()
                    .conNombre("")
                    .build();

            assertThatThrownBy(() -> etiquetaService.crearEtiqueta(etiquetaInvalida, usuario))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("vacío");

            verify(etiquetaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("obtenerEtiquetasPorUsuarioId()")
    class ObtenerEtiquetasTests {

        @Test
        @DisplayName("Debería obtener etiquetas del usuario")
        void deberiaObtenerEtiquetas() {
            Etiqueta etiqueta2 = EtiquetaTestBuilder.unaEtiqueta()
                    .conId(2L)
                    .conNombre("Personal")
                    .conUsuario(usuario)
                    .build();

            when(etiquetaRepository.findByUsuarioId(1L))
                    .thenReturn(List.of(etiqueta, etiqueta2));

            List<Etiqueta> resultado = etiquetaService.obtenerEtiquetasPorUsuarioId(1L);

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting("nombre")
                    .containsExactlyInAnyOrder("Trabajo", "Personal");
        }

        @Test
        @DisplayName("Debería retornar lista vacía si no hay etiquetas")
        void deberiaRetornarListaVacia() {
            when(etiquetaRepository.findByUsuarioId(1L))
                    .thenReturn(List.of());

            List<Etiqueta> resultado = etiquetaService.obtenerEtiquetasPorUsuarioId(1L);

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("actualizarEtiqueta()")
    class ActualizarEtiquetaTests {

        @Test
        @DisplayName("Debería actualizar nombre y color")
        void deberiaActualizarNombreYColor() {
            Etiqueta etiquetaActualizada = EtiquetaTestBuilder.unaEtiqueta()
                    .conNombre("Trabajo Urgente")
                    .conColor("#FF0000")
                    .build();

            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(etiqueta));
            when(etiquetaRepository.existsByUsuarioIdAndNombre(1L, "Trabajo Urgente"))
                    .thenReturn(false);
            when(etiquetaRepository.save(any(Etiqueta.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Etiqueta resultado = etiquetaService.actualizarEtiqueta(1L, etiquetaActualizada, 1L);

            assertThat(resultado.getNombre()).isEqualTo("Trabajo Urgente");
            assertThat(resultado.getColorHex()).isEqualTo("#FF0000");
            verify(etiquetaRepository).save(etiqueta);
        }

        @Test
        @DisplayName("Debería rechazar actualización de otro usuario")
        void deberiaRechazarActualizacionOtroUsuario() {
            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(etiqueta));

            Etiqueta etiquetaActualizada = EtiquetaTestBuilder.unaEtiqueta()
                    .conNombre("Nuevo Nombre")
                    .build();

            assertThatThrownBy(() -> etiquetaService.actualizarEtiqueta(1L, etiquetaActualizada, 999L))
                    .isInstanceOf(UnauthorizedException.class);

            verify(etiquetaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar nombre duplicado en actualización")
        void deberiaRechazarNombreDuplicadoEnActualizacion() {
            Etiqueta etiquetaActualizada = EtiquetaTestBuilder.unaEtiqueta()
                    .conNombre("Personal")
                    .build();

            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(etiqueta));
            when(etiquetaRepository.existsByUsuarioIdAndNombre(1L, "Personal"))
                    .thenReturn(true);

            assertThatThrownBy(() -> etiquetaService.actualizarEtiqueta(1L, etiquetaActualizada, 1L))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("eliminarEtiqueta()")
    class EliminarEtiquetaTests {

        @Test
        @DisplayName("Debería eliminar etiqueta correctamente")
        void deberiaEliminarEtiqueta() {
            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(etiqueta));
            doNothing().when(etiquetaRepository).delete(etiqueta);

            etiquetaService.eliminarEtiqueta(1L, 1L);

            verify(etiquetaRepository).delete(etiqueta);
        }

        @Test
        @DisplayName("Debería rechazar eliminación de otro usuario")
        void deberiaRechazarEliminacionOtroUsuario() {
            when(etiquetaRepository.findById(1L)).thenReturn(Optional.of(etiqueta));

            assertThatThrownBy(() -> etiquetaService.eliminarEtiqueta(1L, 999L))
                    .isInstanceOf(UnauthorizedException.class);

            verify(etiquetaRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Debería rechazar etiqueta inexistente")
        void deberiaRechazarEtiquetaInexistente() {
            when(etiquetaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> etiquetaService.eliminarEtiqueta(999L, 1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}