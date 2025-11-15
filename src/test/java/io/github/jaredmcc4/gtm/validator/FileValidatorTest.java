package io.github.jaredmcc4.gtm.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.*;

@DisplayName("File Validator - Unit Tests")
class FileValidatorTest {

    @Nested
    @DisplayName("validate()")
    class ValidateTests {

        @Test
        @DisplayName("Debería aceptar un archivo PDF válido")
        void deberiaAceptarPDFValido() {

            MultipartFile file = new MockMultipartFile(
                    "file",
                    "documento.pdf",
                    "application/pdf",
                    "contenido".getBytes()
            );

            assertThatCode(() -> FileValidator.validate(file))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debería poder aceptar un archivo de imagen válido")
        void deberiaAceptarImagenValida() {
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "imagen.jpg",
                    "image/jpeg",
                    new byte[1024]
            );

            assertThatCode(() -> FileValidator.validate(file))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debería rechazar un archivo vacío")
        void deberiaRechazarArchivoVacio() {

            MultipartFile file = new MockMultipartFile(
                    "file",
                    "vacio.pdf",
                    "application/pdf",
                    new byte[0]
            );

            assertThatThrownBy(() -> FileValidator.validate(file))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Archivo está vacío");
        }

        @Test
        @DisplayName("Debería rechazar la subida de un archivo mayor a 10MB")
        void deberiaRechazarArchivoMuyGrande() {

            byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "grande.pdf",
                    "application/pdf",
                    largeContent
            );

            assertThatThrownBy(() -> FileValidator.validate(file))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Excede el tamaño máximo permitido");
        }

        @Test
        @DisplayName("Debería rechazar un tipo de archivo no permitido")
        void deberiaRechazarTipoNoPermitido() {

            MultipartFile file = new MockMultipartFile(
                    "file",
                    "ejecutable.exe",
                    "application/x-msdownload",
                    "contenido".getBytes()
            );

            assertThatThrownBy(() -> FileValidator.validate(file))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tipo de archivo no está permitido");
        }

        @Test
        @DisplayName("Debería aceptar subida de archivo en el límite de tamaño (10MB)")
        void deberiaAceptarArchivoEnLimiteTamano() {

            byte[] content = new byte[10 * 1024 * 1024]; // Exactamente 10MB
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "limite.pdf",
                    "application/pdf",
                    content
            );

            assertThatCode(() -> FileValidator.validate(file))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debería aceptar documentos de Office")
        void deberiaAceptarDocumentosOffice() {

            MultipartFile[] files = {
                    new MockMultipartFile("file", "doc.docx",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "content".getBytes()),
                    new MockMultipartFile("file", "sheet.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "content".getBytes()),
                    new MockMultipartFile("file", "presentation.pptx",
                            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                            "content".getBytes())
            };

            for (MultipartFile file : files) {
                assertThatCode(() -> FileValidator.validate(file))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("Debería poder aceptar archivos de código fuente")
        void deberiaAceptarArchivosCodigo() {

            MultipartFile[] files = {
                    new MockMultipartFile("file", "script.js", "text/javascript", "code".getBytes()),
                    new MockMultipartFile("file", "Main.java", "text/x-java-source", "code".getBytes()),
                    new MockMultipartFile("file", "script.py", "text/x-python", "code".getBytes())
            };
            for (MultipartFile file : files) {
                assertThatCode(() -> FileValidator.validate(file))
                        .doesNotThrowAnyException();
            }
        }
    }
}