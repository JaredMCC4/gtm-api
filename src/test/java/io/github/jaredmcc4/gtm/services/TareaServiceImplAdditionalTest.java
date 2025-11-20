package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.builders.TareaTestBuilder;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.repository.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TareaServiceImpl - Cobertura adicional")
class TareaServiceImplAdditionalTest {

    @Mock
    private TareaRepository tareaRepository;

    @InjectMocks
    private TareaServiceImpl tareaService;

    private Usuario usuario;
    private Tarea tarea;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        usuario = UsuarioTestBuilder.unUsuario().conId(1L).build();
        tarea = TareaTestBuilder.unaTarea()
                .conId(50L)
                .conUsuario(usuario)
                .build();
        pageable = PageRequest.of(0, 5);
    }

    @Test
    @DisplayName("obtenerTareasPorUsuarioId debe delegar en el repositorio")
    void deberiaObtenerTareasPorUsuarioId() {
        Page<Tarea> page = new PageImpl<>(List.of(tarea), pageable, 1);
        when(tareaRepository.findByUsuarioId(1L, pageable)).thenReturn(page);

        Page<Tarea> resultado = tareaService.obtenerTareasPorUsuarioId(1L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(tareaRepository).findByUsuarioId(1L, pageable);
    }

    @Test
    @DisplayName("obtenerTareasPorEtiquetaId debe consultar por etiqueta y usuario")
    void deberiaObtenerTareasPorEtiqueta() {
        Page<Tarea> page = new PageImpl<>(List.of(tarea), pageable, 1);
        when(tareaRepository.findByUsuarioIdAndEtiquetaId(1L, 7L, pageable)).thenReturn(page);

        Page<Tarea> resultado = tareaService.obtenerTareasPorEtiquetaId(7L, 1L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(tareaRepository).findByUsuarioIdAndEtiquetaId(1L, 7L, pageable);
    }

    @Test
    @DisplayName("contarTareasPorEstado debe devolver el valor del repositorio")
    void deberiaContarTareasPorEstado() {
        when(tareaRepository.countByUsuarioIdAndEstado(1L, Tarea.EstadoTarea.PENDIENTE)).thenReturn(5L);

        long resultado = tareaService.contarTareasPorEstado(1L, Tarea.EstadoTarea.PENDIENTE);

        assertThat(resultado).isEqualTo(5L);
    }

    @Test
    @DisplayName("validarTarea debe rechazar títulos solo con espacios")
    void deberiaRechazarTituloSoloEspacios() {
        Tarea invalida = TareaTestBuilder.unaTarea()
                .conTitulo("   ")
                .build();

        assertThatThrownBy(() -> tareaService.crearTarea(invalida, usuario))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("actualizarTarea debe mantener los valores si no hay cambios")
    void deberiaMantenerValoresSiNoHayCambios() {
        when(tareaRepository.findByIdAndUsuarioId(50L, 1L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

        Tarea resultado = tareaService.actualizarTarea(50L, Tarea.builder().build(), 1L);

        assertThat(resultado.getTitulo()).isEqualTo(tarea.getTitulo());
        assertThat(resultado.getEstado()).isEqualTo(tarea.getEstado());
    }

    @Test
    @DisplayName("actualizarTarea debe aplicar nueva descripción y fecha de vencimiento")
    void deberiaActualizarDescripcionYFecha() {
        Tarea actualizacion = Tarea.builder()
                .descripcion("Nueva descripción")
                .fechaVencimiento(LocalDateTime.now().plusDays(3))
                .build();

        when(tareaRepository.findByIdAndUsuarioId(50L, 1L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        Tarea resultado = tareaService.actualizarTarea(50L, actualizacion, 1L);

        assertThat(resultado.getDescripcion()).isEqualTo("Nueva descripción");
        assertThat(resultado.getFechaVencimiento()).isEqualTo(actualizacion.getFechaVencimiento());
    }
}
