package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Subtarea;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.SubtareaRepository;
import io.github.jaredmcc4.gtm.repository.TareaRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubtareaServiceImpl - Cobertura adicional")
class SubtareaServiceImplAdditionalTest {

    @Mock
    private SubtareaRepository subtareaRepository;

    @Mock
    private TareaRepository tareaRepository;

    @InjectMocks
    private SubtareaServiceImpl subtareaService;

    private Tarea tarea;

    @BeforeEach
    void setUp() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("user@test.com")
                .contrasenaHash("hash")
                .build();
        tarea = Tarea.builder()
                .id(1L)
                .usuario(usuario)
                .titulo("Principal")
                .build();
    }

    @Test
    @DisplayName("crearSubtarea debe rechazar título null")
    void deberiaRechazarTituloNull() {
        Subtarea invalida = Subtarea.builder().titulo(null).build();
        when(tareaRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(tarea));

        assertThatThrownBy(() -> subtareaService.crearSubtarea(1L, invalida, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("actualizarSubtarea debe rechazar títulos mayores a 120 caracteres")
    void deberiaRechazarActualizacionTituloLargo() {
        Subtarea actualizacion = Subtarea.builder()
                .titulo("a".repeat(121))
                .build();
        Subtarea existente = Subtarea.builder()
                .id(3L)
                .titulo("Original")
                .tarea(tarea)
                .build();

        when(subtareaRepository.findById(3L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> subtareaService.actualizarSubtarea(3L, actualizacion, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("obtenerSubtareaPropia debe fallar si la subtarea no tiene tarea asociada")
    void deberiaFallarSubtareaSinTarea() {
        Subtarea sinTarea = Subtarea.builder()
                .id(5L)
                .titulo("Huérfana")
                .build();

        when(subtareaRepository.findById(5L)).thenReturn(Optional.of(sinTarea));

        assertThatThrownBy(() -> subtareaService.eliminarSubtarea(5L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
