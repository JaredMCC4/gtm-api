package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("Etiqueta Mapper - Unit Tests")
class EtiquetaMapperTest {

    @Autowired
    private EtiquetaMapper etiquetaMapper;

    @Test
    @DisplayName("Debería mapear Etiqueta a EtiquetaDto")
    void deberiaMapeAREtiquetaDto() {

        Etiqueta etiqueta = Etiqueta.builder()
                .id(1L)
                .nombre("Trabajo")
                .colorHex("#FF5733")
                .build();

        EtiquetaDto resultado = etiquetaMapper.toDto(etiqueta);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Trabajo");
        assertThat(resultado.getColorHex()).isEqualTo("#FF5733");
    }

    @Test
    @DisplayName("Debería mapear EtiquetaDto a Etiqueta")
    void deberiaMapeARDtoAEntity() {

        EtiquetaDto dto = EtiquetaDto.builder()
                .id(1L)
                .nombre("Personal")
                .colorHex("#00FF00")
                .build();

        Etiqueta resultado = etiquetaMapper.toEntity(dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Personal");
        assertThat(resultado.getColorHex()).isEqualTo("#00FF00");
    }

    @Test
    @DisplayName("Debería mapear lista de Etiquetas manualmente")
    void deberiaMapeARListaEtiquetas() {

        List<Etiqueta> etiquetas = List.of(
                Etiqueta.builder().id(1L).nombre("Tag1").colorHex("#FF0000").build(),
                Etiqueta.builder().id(2L).nombre("Tag2").colorHex("#00FF00").build()
        );

        List<EtiquetaDto> resultado = etiquetas.stream()
                .map(etiquetaMapper::toDto)
                .collect(Collectors.toList());

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting("nombre")
                .containsExactlyInAnyOrder("Tag1", "Tag2");
        assertThat(resultado).extracting("colorHex")
                .containsExactlyInAnyOrder("#FF0000", "#00FF00");
    }

    @Test
    @DisplayName("Debería retornar null si Etiqueta es null")
    void deberiaRetornarNullSiEtiquetaEsNull() {

        EtiquetaDto resultado = etiquetaMapper.toDto(null);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Debería retornar null si EtiquetaDto es null")
    void deberiaRetornarNullSiDtoEsNull() {

        Etiqueta resultado = etiquetaMapper.toEntity(null);

        assertThat(resultado).isNull();
    }
}