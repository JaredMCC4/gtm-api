package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.builders.TareaTestBuilder;
import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.AdjuntoRepository;
import io.github.jaredmcc4.gtm.repository.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdjuntoService - Unit Tests")
class AdjuntoServiceImplTest {

    @Mock
    private AdjuntoRepository adjuntoRepository;

    @Mock
    private TareaRepository tareaRepository;

    @InjectMocks
    private AdjuntoServiceImpl adjuntoService;

    @TempDir
    Path tempDir;

    private Usuario usuario;
    private Tarea tarea;

    @BeforeEach
    void setUp() {
        usuario = UsuarioTestBuilder.unUsuario().conId(1L).build();
        tarea = TareaTestBuilder.unaTarea()
                .conId(1L)
                .conUsuario(usuario)
                .build();

        ReflectionTestUtils.setField(adjuntoService, "uploadDir", tempDir.toString());
    }

    @Nested
    @DisplayName("subirAdjunto()")
    class SubirAdjuntoTests {

        @Test
        @DisplayName("Debería subir archivo correctamente")
        void deberiaSubirArchivo() throws IOException {
            // Arrange
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "documento.pdf",
                    "application/pdf",
                    "contenido del archivo".getBytes()
            );

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));
            when(adjuntoRepository.save(any(Adjunto.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            Adjunto resultado = adjuntoService.subirAdjunto(file, 1L, 1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNombreArchivo()).isEqualTo("documento.pdf");
            assertThat(resultado.getTipoArchivo()).isEqualTo("application/pdf");
            assertThat(resultado.getRutaArchivo()).contains(tempDir.toString());
            assertThat(Files.exists(Path.of(resultado.getRutaArchivo()))).isTrue();

            verify(adjuntoRepository).save(any(Adjunto.class));
        }

        @Test
        @DisplayName("Debería rechazar archivo vacío")
        void deberiaRechazarArchivoVacio() {
            // Arrange
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "vacio.pdf",
                    "application/pdf",
                    new byte[0]
            );

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));

            // Act & Assert
            assertThatThrownBy(() -> adjuntoService.subirAdjunto(file, 1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("vacío");

            verify(adjuntoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar tipo de archivo no permitido")
        void deberiaRechazarTipoNoPermitido() {
            // Arrange
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "malware.exe",
                    "application/x-msdownload",
                    "contenido".getBytes()
            );

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));

            // Act & Assert
            assertThatThrownBy(() -> adjuntoService.subirAdjunto(file, 1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no está permitido");

            verify(adjuntoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar archivo mayor a 10MB")
        void deberiaRechazarArchivoGrande() {
            // Arrange
            byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "grande.pdf",
                    "application/pdf",
                    largeContent
            );

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));

            // Act & Assert
            assertThatThrownBy(() -> adjuntoService.subirAdjunto(file, 1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tamaño máximo");

            verify(adjuntoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar cuando tarea no existe")
        void deberiaRechazarTareaInexistente() {
            // Arrange
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "doc.pdf",
                    "application/pdf",
                    "contenido".getBytes()
            );

            when(tareaRepository.findByIdAndUsuarioId(999L, 1L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> adjuntoService.subirAdjunto(file, 999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(adjuntoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería sanitizar nombre de archivo con caracteres especiales")
        void deberiaSanitizarNombreArchivo() throws IOException {
            // Arrange
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "archivo con espacios & símbolos!@#.pdf",
                    "application/pdf",
                    "contenido".getBytes()
            );

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));
            when(adjuntoRepository.save(any(Adjunto.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            Adjunto resultado = adjuntoService.subirAdjunto(file, 1L, 1L);

            // Assert
            assertThat(resultado.getRutaArchivo())
                    .doesNotContain(" ", "&", "!", "@", "#");
        }
    }

    @Nested
    @DisplayName("obtenerAdjuntos()")
    class ObtenerAdjuntosTests {

        @Test
        @DisplayName("Debería obtener adjuntos de la tarea")
        void deberiaObtenerAdjuntos() {
            // Arrange
            Adjunto adjunto1 = Adjunto.builder()
                    .id(1L)
                    .nombreArchivo("doc1.pdf")
                    .tarea(tarea)
                    .build();
            Adjunto adjunto2 = Adjunto.builder()
                    .id(2L)
                    .nombreArchivo("doc2.pdf")
                    .tarea(tarea)
                    .build();

            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));
            when(adjuntoRepository.findByTareaId(1L))
                    .thenReturn(List.of(adjunto1, adjunto2));

            // Act
            List<Adjunto> resultado = adjuntoService.obtenerAdjuntos(1L, 1L);

            // Assert
            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting("nombreArchivo")
                    .containsExactly("doc1.pdf", "doc2.pdf");
        }
    }

    @Nested
    @DisplayName("descargarAdjunto()")
    class DescargarAdjuntoTests {

        @Test
        @DisplayName("Debería descargar archivo existente")
        void deberiaDescargarArchivo() throws IOException {
            // Arrange
            Path archivoTest = tempDir.resolve("test.pdf");
            Files.write(archivoTest, "contenido".getBytes());

            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .nombreArchivo("test.pdf")
                    .rutaArchivo(archivoTest.toString())
                    .tarea(tarea)
                    .build();

            when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));
            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));

            // Act
            Resource resultado = adjuntoService.descargarAdjunto(1L, 1L, 1L);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.exists()).isTrue();
            assertThat(resultado.isReadable()).isTrue();
        }

        @Test
        @DisplayName("Debería lanzar excepción si archivo no existe en disco")
        void deberiaLanzarExcepcionSiArchivoNoExiste() {
            // Arrange
            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .nombreArchivo("inexistente.pdf")
                    .rutaArchivo(tempDir.resolve("inexistente.pdf").toString())
                    .tarea(tarea)
                    .build();

            when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));
            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));

            // Act & Assert
            assertThatThrownBy(() -> adjuntoService.descargarAdjunto(1L, 1L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("no se encuentra en el servidor");
        }
    }

    @Nested
    @DisplayName("eliminarAdjunto()")
    class EliminarAdjuntoTests {

        @Test
        @DisplayName("Debería eliminar adjunto y archivo físico")
        void deberiaEliminarAdjuntoYArchivo() throws IOException {
            // Arrange
            Path archivoTest = tempDir.resolve("eliminar.pdf");
            Files.write(archivoTest, "contenido".getBytes());

            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .nombreArchivo("eliminar.pdf")
                    .rutaArchivo(archivoTest.toString())
                    .tarea(tarea)
                    .build();

            when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));
            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));

            // Act
            adjuntoService.eliminarAdjunto(1L, 1L, 1L);

            // Assert
            assertThat(Files.exists(archivoTest)).isFalse();
            verify(adjuntoRepository).delete(adjunto);
        }

        @Test
        @DisplayName("Debería eliminar registro aunque archivo físico no exista")
        void deberiaEliminarRegistroAunqueArchivoNoExista() {
            // Arrange
            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .nombreArchivo("noexiste.pdf")
                    .rutaArchivo(tempDir.resolve("noexiste.pdf").toString())
                    .tarea(tarea)
                    .build();

            when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));
            when(tareaRepository.findByIdAndUsuarioId(1L, 1L))
                    .thenReturn(Optional.of(tarea));

            // Act & Assert
            assertThatCode(() -> adjuntoService.eliminarAdjunto(1L, 1L, 1L))
                    .doesNotThrowAnyException();

            verify(adjuntoRepository).delete(adjunto);
        }
    }
}