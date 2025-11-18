package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Subtarea;
import io.github.jaredmcc4.gtm.dto.subtarea.SubtareaDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("Subtarea Mapper - Unit Tests")
class SubtareaMapperTest {

    @Autowired
    private SubtareaMapper subtareaMapper;

    @Test
    @DisplayName("Debería mapear Subtarea a SubtareaDto")
    void deberiaMapeARSubtareaDto() {
        Subtarea subtarea = Subtarea.builder()
                .id(1L)
                .titulo("Subtarea de prueba")
                .completada(false)
                .build();

        SubtareaDto resultado = subtareaMapper.toDto(subtarea);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitulo()).isEqualTo("Subtarea de prueba");
        assertThat(resultado.getCompletada()).isFalse();
    }

    @Test
    @DisplayName("Debería mapear SubtareaDto a Subtarea")
    void deberiaMapeARDtoAEntity() {

        SubtareaDto dto = SubtareaDto.builder()
                .id(1L)
                .titulo("Nueva subtarea")
                .completada(true)
                .build();

        Subtarea resultado = subtareaMapper.toEntity(dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitulo()).isEqualTo("Nueva subtarea");
        assertThat(resultado.getCompletada()).isTrue();
    }

    @Test
    @DisplayName("Debería mapear lista de Subtareas manualmente")
    void deberiaMapeARListaSubtareaDtos() {

        List<Subtarea> subtareas = List.of(
                Subtarea.builder().id(1L).titulo("Sub 1").completada(false).build(),
                Subtarea.builder().id(2L).titulo("Sub 2").completada(true).build()
        );

        List<SubtareaDto> resultado = subtareas.stream()
                .map(subtareaMapper::toDto)
                .collect(Collectors.toList());

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getTitulo()).isEqualTo("Sub 1");
        assertThat(resultado.get(1).getCompletada()).isTrue();
    }

    @Test
    @DisplayName("Debería manejar subtarea completada por defecto")
    void deberiaManejarCompletadaPorDefecto() {

        SubtareaDto dto = SubtareaDto.builder()
                .titulo("Subtarea sin completar")
                .build();

        Subtarea resultado = subtareaMapper.toEntity(dto);

        assertThat(resultado.getCompletada()).isFalse();
    }

    @Test
    @DisplayName("Debería retornar null si Subtarea es null")
    void deberiaRetornarNullSiSubtareaEsNull() {

        SubtareaDto resultado = subtareaMapper.toDto(null);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Debería retornar null si SubtareaDto es null")
    void deberiaRetornarNullSiDtoEsNull() {

        Subtarea resultado = subtareaMapper.toEntity(null);

        assertThat(resultado).isNull();
    }
}