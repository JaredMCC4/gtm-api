package io.github.jaredmcc4.gtm.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tarea - Builder y colecciones")
class TareaTest {

    @Test
    @DisplayName("El builder inicializa valores por defecto y copia etiquetas")
    void builderDebeInicializarValores() {
        Etiqueta etiqueta = Etiqueta.builder().id(1L).nombre("Work").colorHex("#FFFFFF").build();

        Tarea tarea = Tarea.builder()
                .id(10L)
                .usuario(Usuario.builder().id(5L).email("user@test.com").build())
                .titulo("Preparar informe")
                .etiquetas(Set.of(etiqueta))
                .build();

        assertThat(tarea.getPrioridad()).isEqualTo(Tarea.Prioridad.MEDIA);
        assertThat(tarea.getEstado()).isEqualTo(Tarea.EstadoTarea.PENDIENTE);
        assertThat(tarea.getEtiquetas()).hasSize(1);

        // modificar el set original no debe afectar a la tarea
        Set<Etiqueta> originales = tarea.getEtiquetas();
        originales.clear();
        assertThat(tarea.getEtiquetas()).hasSize(1);
    }

    @Test
    @DisplayName("setEtiquetas debe manejar valores null")
    void setEtiquetasAceptaNull() {
        Tarea tarea = Tarea.builder()
                .usuario(Usuario.builder().id(1L).email("user@test.com").build())
                .titulo("Documento")
                .build();

        tarea.setEtiquetas(null);

        assertThat(tarea.getEtiquetas()).isNotNull();
        assertThat(tarea.getEtiquetas()).isEmpty();
    }
}
