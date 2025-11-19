package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.builders.TareaTestBuilder;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.domain.Subtarea;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.SubtareaRepository;
import io.github.jaredmcc4.gtm.repository.TareaRepository;
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
@DisplayName("Subtarea Service - Unit Tests")
class SubtareaServiceImplTest {

    @Mock
    private SubtareaRepository subtareaRepository;

    @Mock
    private TareaRepository tareaRepository;

    @InjectMocks
    private SubtareaServiceImpl subtareaService;

    private Tarea tarea;
    private Subtarea subtarea;

    @BeforeEach
    void setUp() {
        Usuario usuario = UsuarioTestBuilder.unUsuario().conId(1L).build();
        tarea = TareaTestBuilder.unaTarea()
                .conId(1L)
                .conUsuario(usuario)
                .build();
        subtarea = Subtarea.builder()
                .id(1L)
                .titulo("Subtarea 1")
                .completada(false)
                .tarea(tarea)
                .build();
    }

    @Nested
    @DisplayName("crearSubtarea()")
    class CrearSubtareaTests {

        @Test
        @DisplayName("Debería crear subtarea correctamente")
        void deberiaCrearSubtarea() {
            Subtarea nuevaSubtarea = Subtarea.builder()
                    .titulo("Nueva subtarea")
                    .completada(false)
                    .build();

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));
            when(subtareaRepository.save(any(Subtarea.class)))
                    .thenAnswer(inv -> {
                        Subtarea s = inv.getArgument(0);
                        s.setId(2L);
                        return s;
                    });

            Subtarea resultado = subtareaService.crearSubtarea(1L, nuevaSubtarea, 1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getTitulo()).isEqualTo("Nueva subtarea");
            assertThat(resultado.getCompletada()).isFalse();
            assertThat(resultado.getTarea()).isEqualTo(tarea);

            verify(subtareaRepository).save(any(Subtarea.class));
        }

        @Test
        @DisplayName("Debería rechazar tarea inexistente")
        void deberiaRechazarTareaInexistente() {
            when(tareaRepository.findByIdAndUsuarioId(999L, 1L))
                    .thenReturn(Optional.empty());

            Subtarea nuevaSubtarea = Subtarea.builder()
                    .titulo("Test")
                    .build();

            assertThatThrownBy(() -> subtareaService.crearSubtarea(999L, nuevaSubtarea, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("no encontrada");

            verify(subtareaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar título vacío")
        void deberiaRechazarTituloVacio() {
            Subtarea subtareaInvalida = Subtarea.builder()
                    .titulo("")
                    .build();

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));

            assertThatThrownBy(() -> subtareaService.crearSubtarea(1L, subtareaInvalida, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("vacío");

            verify(subtareaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar título muy largo")
        void deberiaRechazarTituloMuyLargo() {
            String tituloLargo = "a".repeat(121);
            Subtarea subtareaInvalida = Subtarea.builder()
                    .titulo(tituloLargo)
                    .build();

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));

            assertThatThrownBy(() -> subtareaService.crearSubtarea(1L, subtareaInvalida, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("120 caracteres");

            verify(subtareaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("mostrarSubtareas()")
    class MostrarSubtareasTests {

        @Test
        @DisplayName("Debería obtener subtareas de la tarea")
        void deberiaObtenerSubtareas() {
            Subtarea subtarea2 = Subtarea.builder()
                    .id(2L)
                    .titulo("Subtarea 2")
                    .completada(true)
                    .tarea(tarea)
                    .build();

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));
            when(subtareaRepository.findByTareaId(1L))
                    .thenReturn(List.of(subtarea, subtarea2));

            List<Subtarea> resultado = subtareaService.mostrarSubtareas(1L, 1L);

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting("titulo")
                    .containsExactlyInAnyOrder("Subtarea 1", "Subtarea 2");
        }

        @Test
        @DisplayName("Debería retornar lista vacía si no hay subtareas")
        void deberiaRetornarListaVacia() {
            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));
            when(subtareaRepository.findByTareaId(1L))
                    .thenReturn(List.of());

            List<Subtarea> resultado = subtareaService.mostrarSubtareas(1L, 1L);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Debería rechazar tarea de otro usuario")
        void deberiaRechazarTareaOtroUsuario() {
            when(tareaRepository.findByIdAndUsuarioId(1L, 999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> subtareaService.mostrarSubtareas(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("actualizarSubtarea()")
    class ActualizarSubtareaTests {

        @Test
        @DisplayName("Debería actualizar título y estado")
        void deberiaActualizarTituloYEstado() {
            Subtarea actualizacion = Subtarea.builder()
                    .titulo("Título actualizado")
                    .completada(true)
                    .build();

            when(subtareaRepository.findById(1L))
                    .thenReturn(Optional.of(subtarea));
            when(subtareaRepository.save(any(Subtarea.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Subtarea resultado = subtareaService.actualizarSubtarea(1L, actualizacion, 1L);

            assertThat(resultado.getTitulo()).isEqualTo("Título actualizado");
            assertThat(resultado.getCompletada()).isTrue();
            verify(subtareaRepository).save(subtarea);
        }

        @Test
        @DisplayName("Debería actualizar solo título")
        void deberiaActualizarSoloTitulo() {
            Subtarea actualizacion = Subtarea.builder()
                    .titulo("Solo título")
                    .build();

            when(subtareaRepository.findById(1L))
                    .thenReturn(Optional.of(subtarea));
            when(subtareaRepository.save(any(Subtarea.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Subtarea resultado = subtareaService.actualizarSubtarea(1L, actualizacion, 1L);

            assertThat(resultado.getTitulo()).isEqualTo("Solo título");
            assertThat(resultado.getCompletada()).isFalse(); // No cambió
        }

        @Test
        @DisplayName("Debería rechazar actualización de otro usuario")
        void deberiaRechazarActualizacionOtroUsuario() {

            when(subtareaRepository.findById(1L))
                    .thenReturn(Optional.of(subtarea));

            Subtarea subtareaActualizada = Subtarea.builder()
                    .titulo("Nuevo título")
                    .build();

            assertThatThrownBy(() -> subtareaService.actualizarSubtarea(1L, subtareaActualizada, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .satisfies(ex -> {
                        String message = ex.getMessage().toLowerCase();
                        assertThat(message).contains("no encontrada");
                        assertThat(message).contains("no pertenece al usuario");
                    });
        }
    }

    @Nested
    @DisplayName("eliminarSubtarea()")
    class EliminarSubtareaTests {

        @Test
        @DisplayName("Debería eliminar subtarea correctamente")
        void deberiaEliminarSubtarea() {

            when(subtareaRepository.findById(1L))
                    .thenReturn(Optional.of(subtarea));

            subtareaService.eliminarSubtarea(1L, 1L);

            verify(subtareaRepository).delete(subtarea);
        }

        @Test
        @DisplayName("Debería rechazar eliminación de otro usuario")
        void deberiaRechazarEliminacionOtroUsuario() {

            when(subtareaRepository.findById(1L))
                    .thenReturn(Optional.of(subtarea));

            assertThatThrownBy(() -> subtareaService.eliminarSubtarea(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .satisfies(ex -> {
                        String message = ex.getMessage().toLowerCase();
                        assertThat(message).contains("no encontrada");
                        assertThat(message).contains("no pertenece al usuario");
                    });
        }

        @Test
        @DisplayName("Debería rechazar subtarea inexistente")
        void deberiaRechazarSubtareaInexistente() {
            when(subtareaRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> subtareaService.eliminarSubtarea(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}