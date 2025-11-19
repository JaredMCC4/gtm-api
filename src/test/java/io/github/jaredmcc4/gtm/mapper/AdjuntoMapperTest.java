package io.github.jaredmcc4.gtm.mapper;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.dto.adjunto.AdjuntoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Adjunto Mapper - Unit Tests")
class AdjuntoMapperTest {

    private final AdjuntoMapper adjuntoMapper = new AdjuntoMapper();

    @Test
    @DisplayName("Debería mapear Adjunto a AdjuntoDto")
    void deberiaMapeARAdjuntoDto() {
        LocalDateTime now = LocalDateTime.now();
        Adjunto adjunto = Adjunto.builder()
                .id(1L)
                .nombre("documento.pdf")
                .path("/uploads/documento.pdf")
                .mimeType("application/pdf")
                .sizeBytes(1024L)
                .uploadedAt(now)
                .build();

        AdjuntoDto resultado = adjuntoMapper.toDto(adjunto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("documento.pdf");
        assertThat(resultado.getMimeType()).isEqualTo("application/pdf");
        assertThat(resultado.getSizeBytes()).isEqualTo(1024L);
        assertThat(resultado.getPath()).isEqualTo("/uploads/documento.pdf");
        assertThat(resultado.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Debería mapear Adjunto sin fechas")
    void deberiaMapeARAdjuntoSinFechas() {

        Adjunto adjunto = Adjunto.builder()
                .id(2L)
                .nombre("imagen.png")
                .mimeType("image/png")
                .sizeBytes(2048L)
                .path("/uploads/imagen.png")
                .build();

        AdjuntoDto resultado = adjuntoMapper.toDto(adjunto);
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(2L);
        assertThat(resultado.getNombre()).isEqualTo("imagen.png");
        assertThat(resultado.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Debería mapear lista de Adjuntos manualmente")
    void deberiaMapeARListaAdjuntos() {

        List<Adjunto> adjuntos = List.of(
                Adjunto.builder().id(1L).nombre("doc1.pdf").mimeType("application/pdf").sizeBytes(100L).build(),
                Adjunto.builder().id(2L).nombre("doc2.pdf").mimeType("application/pdf").sizeBytes(200L).build()
        );

        List<AdjuntoDto> resultado = adjuntos.stream()
                .map(adjuntoMapper::toDto)
                .collect(Collectors.toList());

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("doc1.pdf");
        assertThat(resultado.get(1).getNombre()).isEqualTo("doc2.pdf");
        assertThat(resultado).extracting("sizeBytes")
                .containsExactly(100L, 200L);
    }

    @Test
    @DisplayName("Debería retornar null si Adjunto es null")
    void deberiaRetornarNullSiAdjuntoEsNull() {

        AdjuntoDto resultado = adjuntoMapper.toDto(null);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Debería mapear correctamente el path del archivo")
    void deberiaMapeARPathCorrectamente() {

        Adjunto adjunto = Adjunto.builder()
                .id(3L)
                .nombre("reporte.xlsx")
                .path("/uploads/tareas/1/reporte.xlsx")
                .mimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .sizeBytes(5120L)
                .build();

        AdjuntoDto resultado = adjuntoMapper.toDto(adjunto);

        assertThat(resultado.getPath()).isEqualTo("/uploads/tareas/1/reporte.xlsx");
        assertThat(resultado.getNombre()).isEqualTo("reporte.xlsx");
    }
}