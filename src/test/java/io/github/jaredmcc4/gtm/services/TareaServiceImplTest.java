package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.builders.TareaTestBuilder;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tarea Service - Unit Tests")
class TareaServiceImplTest {

    @Mock
    private TareaRepository tareaRepository;

    @InjectMocks
    private TareaServiceImpl tareaService;

    private Usuario usuario;
    private Tarea tareaBase;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        usuario = UsuarioTestBuilder.unUsuario().conId(1L).build();
        tareaBase = TareaTestBuilder.unaTarea()
                .conId(1L)
                .conUsuario(usuario)
                .conTitulo("Tarea de prueba")
                .build();
        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("crearTarea()")
    class CrearTareaTests {

        @Test
        @DisplayName("Debería crear la tarea sin ningún error")
        void deberiaCrearTareaCorrectamente() {
            Tarea nuevaTarea = TareaTestBuilder.unaTarea()
                    .conTitulo("Nueva tarea")
                    .build();
            when(tareaRepository.save(any(Tarea.class))).thenReturn(nuevaTarea);

            Tarea resultado = tareaService.crearTarea(nuevaTarea, usuario);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getUsuario()).isEqualTo(usuario);
            verify(tareaRepository).save(argThat(tarea ->
                    tarea.getUsuario().equals(usuario)
            ));
        }

        @Test
        @DisplayName("Debería rechazar la tarea si tiene un título vacío")
        void deberiaRechazarTareaSinTitulo() {
            // Arrange
            Tarea tareaSinTitulo = TareaTestBuilder.unaTarea()
                    .conTitulo("")
                    .build();

            assertThatThrownBy(() -> tareaService.crearTarea(tareaSinTitulo, usuario))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Título de la tarea no puede estar vacío");

            verify(tareaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar un título que sea menor que 3 caracteres")
        void deberiaRechazarTituloCorto() {
            Tarea tareaConTituloCorto = TareaTestBuilder.unaTarea()
                    .conTitulo("ab")
                    .build();

            assertThatThrownBy(() -> tareaService.crearTarea(tareaConTituloCorto, usuario))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Entre 3 y 120 caracteres");
            verify(tareaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar un título que sea mayor a 120 caracteres")
        void deberiaRechazarTituloLargo() {
            String tituloLargo = "a".repeat(121);
            Tarea tareaConTituloLargo = TareaTestBuilder.unaTarea()
                    .conTitulo(tituloLargo)
                    .build();

            assertThatThrownBy(() -> tareaService.crearTarea(tareaConTituloLargo, usuario))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Entre 3 y 120 caracteres");
            verify(tareaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("obtenerTareaPorIdYUsuarioId()")
    class ObtenerTareaPorIdTests {
        @Test
        @DisplayName("Debería retornar una tarea cuando pertenezca al usuario")
        void deberiaRetornarTareaSiPerteneceAlUsuario() {
            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tareaBase));
            Tarea resultado = tareaService.obtenerTareaPorIdYUsuarioId(1L, 1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            verify(tareaRepository).findByIdAndUsuarioId(1L, 1L);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando una tarea no pertenezca al usuario")
        void deberiaLanzarExcepcionCuandoTareaNoPertenece() {
            when(tareaRepository.findByIdAndUsuarioId(1L, 999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tareaService.obtenerTareaPorIdYUsuarioId(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("No encontrada o no pertenece al usuario");

            verify(tareaRepository).findByIdAndUsuarioId(1L, 999L);
        }
    }

    @Nested
    @DisplayName("filtrarTareas()")
    class FiltrarTareasTests {

        @Test
        @DisplayName("Debería filtrar por estado correctamente")
        void deberiaFiltrarPorEstado() {
            List<Tarea> tareas = List.of(tareaBase);
            Page<Tarea> page = new PageImpl<>(tareas, pageable, 1);

            when(tareaRepository.findByFilters(
                    eq(1L),
                    eq(Tarea.EstadoTarea.PENDIENTE),
                    isNull(),
                    isNull(),
                    any(Pageable.class)
            )).thenReturn(page);
            Page<Tarea> resultado = tareaService.filtrarTareas(
                    1L,
                    Tarea.EstadoTarea.PENDIENTE,
                    null,
                    null,
                    pageable
            );

            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getEstado())
                    .isEqualTo(Tarea.EstadoTarea.PENDIENTE);
        }

        @Test
        @DisplayName("Debería poder filtrar por prioridad correctamente")
        void deberiaFiltrarPorPrioridad() {
            Tarea tareaAlta = TareaTestBuilder.unaTarea()
                    .conPrioridad(Tarea.Prioridad.ALTA)
                    .build();
            Page<Tarea> page = new PageImpl<>(List.of(tareaAlta), pageable, 1);

            when(tareaRepository.findByFilters(
                    eq(1L),
                    isNull(),
                    eq(Tarea.Prioridad.ALTA),
                    isNull(),
                    any(Pageable.class)
            )).thenReturn(page);

            Page<Tarea> resultado = tareaService.filtrarTareas(
                    1L,
                    null,
                    null,
                    Tarea.Prioridad.ALTA,
                    pageable
            );

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getPrioridad())
                    .isEqualTo(Tarea.Prioridad.ALTA);
        }
    }

    @Nested
    @DisplayName("obtenerTareasProximasVencimiento()")
    class TareasProximasVencerTests {
        @Test
        @DisplayName("Debería retornar las tareas que venzan en los próximos días")
        void deberiaRetornarTareasProximas() {
            Tarea tareaProxima = TareaTestBuilder.unaTarea()
                    .venceEn(3)
                    .conEstado(Tarea.EstadoTarea.PENDIENTE)
                    .build();

            when(tareaRepository.findProximasVencer(
                    eq(1L),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).thenReturn(List.of(tareaProxima));

            List<Tarea> resultado = tareaService.obtenerTareasProximasVencimiento(1L, 7);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getFechaVencimiento())
                    .isBetween(LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        }

        @Test
        @DisplayName("No debería retornar las tareas completadas")
        void noDeberiaRetornarTareasCompletadas() {
            when(tareaRepository.findProximasVencer(
                    eq(1L),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).thenReturn(List.of());

            List<Tarea> resultado = tareaService.obtenerTareasProximasVencimiento(1L, 7);
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("actualizarTarea()")
    class ActualizarTareaTests {

        @Test
        @DisplayName("Debería actualizar todos los campos que sean modificables")
        void deberiaActualizarTodosLosCampos() {
            Tarea tareaActualizada = TareaTestBuilder.unaTarea()
                    .conTitulo("Título actualizado")
                    .conPrioridad(Tarea.Prioridad.ALTA)
                    .conEstado(Tarea.EstadoTarea.COMPLETADA)
                    .venceEn(14)
                    .build();

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tareaBase));
            when(tareaRepository.save(any(Tarea.class))).thenReturn(tareaBase);

            Tarea resultado = tareaService.actualizarTarea(1L, tareaActualizada, 1L);

            verify(tareaRepository).save(argThat(tarea ->
                    tarea.getTitulo().equals("Título actualizado") &&
                            tarea.getPrioridad() == Tarea.Prioridad.ALTA &&
                            tarea.getEstado() == Tarea.EstadoTarea.COMPLETADA
            ));
        }
    }

    @Nested
    @DisplayName("eliminarTarea()")
    class EliminarTareaTests {

        @Test
        @DisplayName("Debería eliminar una tarea del usuario")
        void deberiaEliminarTarea() {
            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tareaBase));

            tareaService.eliminarTarea(1L, 1L);

            verify(tareaRepository).delete(tareaBase);
        }

        @Test
        @DisplayName("Debería lanzar una excepción al eliminar la tarea de otro usuario")
        void deberiaLanzarExcepcionAlEliminarTareaAjena() {
            when(tareaRepository.findByIdAndUsuarioId(1L, 999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tareaService.eliminarTarea(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(tareaRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("buscarTareasPorTexto()")
    class BuscarTareasPorTextoTests {

        @Test
        @DisplayName("Debería buscar por el título y la descripción")
        void deberiaBuscarEnTituloYDescripcion() {
            Page<Tarea> page = new PageImpl<>(List.of(tareaBase), pageable, 1);
            when(tareaRepository.searchByTexto(eq(1L), eq("prueba"), any(Pageable.class)))
                    .thenReturn(page);

            Page<Tarea> resultado = tareaService.buscarTareasPorTexto(1L, "prueba", pageable);

            assertThat(resultado.getContent()).hasSize(1);
            verify(tareaRepository).searchByTexto(1L, "prueba", pageable);
        }
    }
}