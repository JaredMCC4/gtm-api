package io.github.jaredmcc4.gtm.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileValidator - Unit Tests")
class FileValidatorTest {

    @Test
    @DisplayName("Debe aceptar archivos válidos")
    void deberiaAceptarArchivoValido() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "doc.pdf",
                "application/pdf",
                "contenido".getBytes()
        );

        assertThatCode(() -> FileValidator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Debe rechazar archivo vacío")
    void deberiaRechazarArchivoVacio() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vacio.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThatThrownBy(() -> FileValidator.validate(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vac");
    }

    @Test
    @DisplayName("Debe rechazar archivos con tamaño mayor al permitido")
    void deberiaRechazarArchivoMuyGrande() {
        byte[] contenido = new byte[11 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "big.pdf",
                "application/pdf",
                contenido
        );

        assertThatThrownBy(() -> FileValidator.validate(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("10MB");
    }

    @Test
    @DisplayName("Debe rechazar tipos de archivo no permitidos")
    void deberiaRechazarTipoNoPermitido() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.sh",
                "application/x-sh",
                "echo test".getBytes()
        );

        assertThatThrownBy(() -> FileValidator.validate(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo de archivo");
    }
}
