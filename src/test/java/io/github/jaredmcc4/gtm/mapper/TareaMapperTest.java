package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
import io.github.jaredmcc4.gtm.dto.tarea.TareaDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("TareaMapper - Unit Tests")
@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class TareaMapperTest {

    @Mock
    private EtiquetaMapper etiquetaMapper;

    @InjectMocks
    private TareaMapper tareaMapper;

    @Test
    @DisplayName("Debe mapear una tarea completa a DTO")
    void deberiaMapearTarea() {
        LocalDateTime now = LocalDateTime.now();
        Etiqueta etiqueta = Etiqueta.builder().id(1L).nombre("Work").colorHex("#FFFFFF").build();
        Tarea tarea = Tarea.builder()
                .id(10L)
                .usuario(Usuario.builder().id(5L).build())
                .titulo("Revisar PR")
                .descripcion("Revisar los cambios pendientes")
                .prioridad(Tarea.Prioridad.ALTA)
                .estado(Tarea.EstadoTarea.PENDIENTE)
                .fechaVencimiento(now.plusDays(1))
                .etiquetas(Set.of(etiqueta))
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();

        when(etiquetaMapper.toDto(any(Etiqueta.class)))
                .thenReturn(EtiquetaDto.builder().id(1L).nombre("Work").colorHex("#FFFFFF").build());

        TareaDto dto = tareaMapper.toDto(tarea);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitulo()).isEqualTo("Revisar PR");
        assertThat(dto.getFechaVencimiento()).isEqualTo(tarea.getFechaVencimiento().toString());
        assertThat(dto.getEtiquetas()).hasSize(1);
    }

    @Test
    @DisplayName("Debe devolver null cuando la tarea sea null")
    void deberiaRetornarNull() {
        assertThat(tareaMapper.toDto(null)).isNull();
    }
}
