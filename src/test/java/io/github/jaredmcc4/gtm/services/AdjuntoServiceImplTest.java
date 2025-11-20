package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.repository.AdjuntoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.mockito.ArgumentCaptor;
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
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Adjunto Service - Unit Tests (alineado a implementación)")
class AdjuntoServiceImplTest {
    @Mock
    private AdjuntoRepository adjuntoRepository;

    @Mock
    private TareaService tareaService;

    @InjectMocks
    private AdjuntoServiceImpl adjuntoService;

    @TempDir
    Path tempDir;

    private Usuario usuario;
    private Tarea tarea;
    private MultipartFile archivo;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(adjuntoService, "uploadDir", tempDir.toString());

        usuario = Usuario.builder().id(1L).build();
        tarea = Tarea.builder().id(1L).usuario(usuario).build();

        archivo = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "contenido del archivo".getBytes()
        );
    }

    @Nested
    @DisplayName("subirAdjunto() - Casos adicionales")
    class SubirAdjuntoCasosAdicionales {

        @Test
        @DisplayName("Debe generar nombre válido cuando el archivo no tiene extensión")
        void deberiaGenerarNombreParaArchivoSinExtension() throws IOException {
            ReflectionTestUtils.setField(adjuntoService, "uploadDir", tempDir.toString());
            when(tareaService.obtenerTareaPorIdYUsuarioId(1L, 1L)).thenReturn(tarea);
            MultipartFile sinExtension = new MockMultipartFile(
                    "file",
                    "archivo",
                    "application/pdf",
                    "contenido".getBytes()
            );
            when(adjuntoRepository.save(any(Adjunto.class))).thenAnswer(inv -> inv.getArgument(0));

            Adjunto adjunto = adjuntoService.subirAdjunto(1L, sinExtension, 1L);

            assertThat(adjunto.getPath())
                    .isNotBlank()
                    .doesNotEndWith(".");
        }
    }

    @Nested
    @DisplayName("subirAdjunto()")
    class SubirAdjuntoTests {

        @Test
        @DisplayName("Debería guardar adjunto correctamente")
        void deberiaSubirAdjunto() throws IOException {
            when(tareaService.obtenerTareaPorIdYUsuarioId(1L, 1L)).thenReturn(tarea);
            when(adjuntoRepository.save(any(Adjunto.class))).thenAnswer(inv -> inv.getArgument(0));

            Adjunto resultado = adjuntoService.subirAdjunto(1L, archivo, 1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNombre()).isEqualTo("documento.pdf");
            assertThat(resultado.getMimeType()).isEqualTo("application/pdf");
            assertThat(resultado.getSizeBytes()).isEqualTo(archivo.getSize());
            assertThat(resultado.getTarea()).isEqualTo(tarea);

            assertThat(Files.exists(Paths.get(resultado.getPath()))).isTrue();
            verify(adjuntoRepository).save(any(Adjunto.class));
        }

        @Test
        @DisplayName("Debería rechazar archivo vacío")
        void deberiaRechazarArchivoVacio() {
            MultipartFile archivoVacio = new MockMultipartFile(
                    "file",
                    "empty.pdf",
                    "application/pdf",
                    new byte[0]
            );

            assertThatThrownBy(() -> adjuntoService.subirAdjunto(1L, archivoVacio, 1L))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(adjuntoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar tarea inexistente")
        void deberiaRechazarTareaInexistente() {
            when(tareaService.obtenerTareaPorIdYUsuarioId(999L, 1L))
                    .thenThrow(new ResourceNotFoundException("Tarea no encontrada"));

            assertThatThrownBy(() -> adjuntoService.subirAdjunto(999L, archivo, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(adjuntoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería generar nombre único (paths distintos) para archivos")
        void deberiaGenerarNombreUnico() throws IOException {
            when(tareaService.obtenerTareaPorIdYUsuarioId(1L, 1L)).thenReturn(tarea);
            ArgumentCaptor<Adjunto> captor = ArgumentCaptor.forClass(Adjunto.class);
            when(adjuntoRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            adjuntoService.subirAdjunto(1L, archivo, 1L);
            adjuntoService.subirAdjunto(1L, archivo, 1L);

            List<Adjunto> adjuntos = captor.getAllValues();
            assertThat(adjuntos).hasSize(2);
            assertThat(adjuntos.get(0).getPath())
                    .isNotBlank()
                    .isNotEqualTo(adjuntos.get(1).getPath());
        }
    }

    @Nested
    @DisplayName("mostrarAdjuntos()")
    class MostrarAdjuntosTests {

        @Test
        @DisplayName("Debería obtener adjuntos de una tarea")
        void deberiaObtenerAdjuntos() {
            Adjunto adjunto1 = Adjunto.builder()
                    .id(1L)
                    .nombre("archivo1.pdf")
                    .tarea(tarea)
                    .build();

            Adjunto adjunto2 = Adjunto.builder()
                    .id(2L)
                    .nombre("archivo2.pdf")
                    .tarea(tarea)
                    .build();

            when(tareaService.obtenerTareaPorIdYUsuarioId(1L, 1L)).thenReturn(tarea);
            when(adjuntoRepository.findByTareaId(1L)).thenReturn(List.of(adjunto1, adjunto2));

            List<Adjunto> resultado = adjuntoService.mostrarAdjuntos(1L, 1L);

            assertThat(resultado).hasSize(2).containsExactly(adjunto1, adjunto2);
        }

        @Test
        @DisplayName("Debería retornar lista vacía si no hay adjuntos")
        void deberiaRetornarListaVacia() {
            when(tareaService.obtenerTareaPorIdYUsuarioId(1L, 1L)).thenReturn(tarea);
            when(adjuntoRepository.findByTareaId(1L)).thenReturn(List.of());

            List<Adjunto> resultado = adjuntoService.mostrarAdjuntos(1L, 1L);
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("descargarAdjunto()")
    class DescargarAdjuntoTests {

        @Test
        @DisplayName("Debería cargar adjunto como Resource")
        void deberiaDescargarAdjunto() throws IOException {

            Path userDir = tempDir.resolve("1");
            Files.createDirectories(userDir);
            Path filePath = userDir.resolve("test-file.pdf");
            Files.write(filePath, "contenido".getBytes());

            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .path(filePath.toString())
                    .nombre("documento.pdf")
                    .tarea(tarea) // tarea con usuario id=1L
                    .build();

            when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));

            Resource resultado = adjuntoService.descargarAdjunto(1L, 1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.exists()).isTrue();
            assertThat(resultado.isReadable()).isTrue();
        }

        @Test
        @DisplayName("Debería rechazar adjunto inexistente")
        void deberiaRechazarAdjuntoInexistente() {
            when(adjuntoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adjuntoService.descargarAdjunto(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Debería rechazar acceso de otro usuario")
        void deberiaRechazarAccesoOtroUsuario() {
            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .path(tempDir.resolve("1").resolve("file.pdf").toString())
                    .tarea(tarea) // usuario id=1L
                    .build();

            when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));

            assertThatThrownBy(() -> adjuntoService.descargarAdjunto(1L, 2L))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    @Nested
    @DisplayName("obtenerAdjuntoPorId()")
    class ObtenerAdjuntoPorIdTests {

        @Test
        @DisplayName("Debe retornar el adjunto cuando pertenece al usuario")
        void deberiaRetornarAdjuntoPropio() {
            Adjunto adjunto = Adjunto.builder()
                    .id(7L)
                    .tarea(tarea)
                    .path(tempDir.resolve("path").toString())
                    .build();

            when(adjuntoRepository.findById(7L)).thenReturn(Optional.of(adjunto));

            Adjunto resultado = adjuntoService.obtenerAdjuntoPorId(7L, 1L);

            assertThat(resultado).isSameAs(adjunto);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el adjunto no existe")
        void deberiaLanzarExcepcionCuandoNoExiste() {
            when(adjuntoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adjuntoService.obtenerAdjuntoPorId(99L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Debe rechazar adjunto de otro usuario")
        void deberiaRechazarAdjuntoAjeno() {
            Usuario otroUsuario = Usuario.builder().id(2L).build();
            Adjunto adjunto = Adjunto.builder()
                    .id(7L)
                    .tarea(Tarea.builder().id(1L).usuario(otroUsuario).build())
                    .build();

            when(adjuntoRepository.findById(7L)).thenReturn(Optional.of(adjunto));

            assertThatThrownBy(() -> adjuntoService.obtenerAdjuntoPorId(7L, 1L))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    @Nested
    @DisplayName("descargarAdjunto() - Casos adicionales")
    class DescargarAdjuntoCasosAdicionales {

        @Test
        @DisplayName("Debe fallar cuando el archivo físico no existe")
        void deberiaFallarSiArchivoNoExiste() {
            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .path(tempDir.resolve("no-existe").resolve("archivo.pdf").toString())
                    .tarea(tarea)
                    .build();

            when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));

            assertThatThrownBy(() -> adjuntoService.descargarAdjunto(1L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("eliminarAdjunto()")
    class EliminarAdjuntoTests {

        @Test
        @DisplayName("Debería eliminar adjunto correctamente")
        void deberiaEliminarAdjunto() throws IOException {
            Path userDir = tempDir.resolve("1");
            Files.createDirectories(userDir);
            Path filePath = userDir.resolve("test-file.pdf");
            Files.write(filePath, "contenido".getBytes());

            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .path(filePath.toString())
                    .nombre("documento.pdf")
                    .tarea(tarea)
                    .build();

            when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));
            doNothing().when(adjuntoRepository).delete(adjunto);

            adjuntoService.eliminarAdjunto(1L, 1L);

            verify(adjuntoRepository).delete(adjunto);
            assertThat(Files.exists(filePath)).isFalse();
        }

        @Test
        @DisplayName("Debería rechazar eliminar adjunto de otro usuario")
        void deberiaRechazarEliminarAdjuntoAjeno() {
            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .path(tempDir.resolve("1").resolve("file.pdf").toString())
                    .tarea(tarea)
                    .build();

            when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));

            assertThatThrownBy(() -> adjuntoService.eliminarAdjunto(1L, 2L))
                    .isInstanceOf(UnauthorizedException.class);

            verify(adjuntoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("No debería fallar si el archivo físico no existe")
        void noDeberiaFallarSiArchivoNoExiste() {
            Adjunto adjunto = Adjunto.builder()
                    .id(1L)
                    .path(tempDir.resolve("1").resolve("non-existent.pdf").toString())
                    .tarea(tarea)
                    .build();

            when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));
            doNothing().when(adjuntoRepository).delete(adjunto);

            assertThatCode(() -> adjuntoService.eliminarAdjunto(1L, 1L))
                    .doesNotThrowAnyException();

            verify(adjuntoRepository).delete(adjunto);
        }
    }
}
