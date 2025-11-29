package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.flyway.enabled=false"
})
@DisplayName("AdjuntoRepository - Integration Tests")
class AdjuntoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdjuntoRepository adjuntoRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Usuario usuario;
    private Tarea tarea;

    @BeforeEach
    void setUp() {
        Rol rolUser = rolRepository.findByNombreRol("USER")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombreRol("USER").build()));

        usuario = Usuario.builder()
                .email("test@example.com")
                .contrasenaHash("$2a$12$hash")
                .nombreUsuario("Usuario Test")
                .activo(true)
                .roles(Set.of(rolUser))
                .build();
        usuario = usuarioRepository.save(usuario);

        tarea = Tarea.builder()
                .titulo("Tarea Principal")
                .descripcion("Descripción")
                .estado(Tarea.EstadoTarea.PENDIENTE)
                .prioridad(Tarea.Prioridad.MEDIA)
                .usuario(usuario)
                .build();
        tarea = tareaRepository.save(tarea);
        entityManager.flush();
    }

    @Nested
    @DisplayName("findByTareaId()")
    class FindByTareaIdTests {

        @Test
        @DisplayName("Debería encontrar adjuntos de una tarea")
        void deberiaEncontrarAdjuntos() {
            crearAdjunto("archivo1.pdf", "/uploads/uuid-1.pdf");
            crearAdjunto("archivo2.pdf", "/uploads/uuid-2.pdf");
            entityManager.flush();

            List<Adjunto> resultado = adjuntoRepository.findByTareaId(tarea.getId());

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting("nombre")
                    .containsExactlyInAnyOrder("archivo1.pdf", "archivo2.pdf");
        }

        @Test
        @DisplayName("Debería retornar lista vacía si no hay adjuntos")
        void deberiaRetornarListaVacia() {
            List<Adjunto> resultado = adjuntoRepository.findByTareaId(tarea.getId());

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("No debería retornar adjuntos de otras tareas")
        void noDeberiaRetornarAdjuntosDeOtrasTareas() {
            Tarea otraTarea = Tarea.builder()
                    .titulo("Otra Tarea")
                    .descripcion("Descripción")
                    .estado(Tarea.EstadoTarea.PENDIENTE)
                    .prioridad(Tarea.Prioridad.BAJA)
                    .usuario(usuario)
                    .build();
            otraTarea = tareaRepository.save(otraTarea);

            crearAdjunto("mi-archivo.pdf", "/uploads/uuid-1.pdf");
            crearAdjuntoParaTarea("otro-archivo.pdf", "/uploads/uuid-2.pdf", otraTarea);
            entityManager.flush();

            List<Adjunto> resultado = adjuntoRepository.findByTareaId(tarea.getId());

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("mi-archivo.pdf");
        }
    }

    @Nested
    @DisplayName("findByIdAndTareaUsuarioId()")
    class FindByIdAndTareaUsuarioIdTests {

        @Test
        @DisplayName("Debería encontrar adjunto del usuario")
        void deberiaEncontrarAdjuntoDelUsuario() {
            Adjunto adjunto = crearAdjunto("archivo.pdf", "/uploads/uuid.pdf");
            entityManager.flush();

            Optional<Adjunto> resultado = adjuntoRepository.findByIdAndTareaUsuarioId(
                    adjunto.getId(), usuario.getId()
            );

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNombre()).isEqualTo("archivo.pdf");
        }

        @Test
        @DisplayName("No debería encontrar adjunto de otro usuario")
        void noDeberiaEncontrarAdjuntoDeOtroUsuario() {
            Adjunto adjunto = crearAdjunto("archivo.pdf", "/uploads/uuid.pdf");
            entityManager.flush();

            Optional<Adjunto> resultado = adjuntoRepository.findByIdAndTareaUsuarioId(
                    adjunto.getId(), 999L
            );

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("Operaciones de cascada")
    class OperacionesCascadaTests {

        @Test
        @DisplayName("Debería eliminar adjuntos al eliminar tarea usando deleteByTareaId")
        void deberiaEliminarAdjuntosAlEliminarTarea() {
            Adjunto adjunto = crearAdjunto("archivo.pdf", "/uploads/uuid.pdf");
            Long adjuntoId = adjunto.getId();

            entityManager.flush();
            entityManager.clear();

            // Primero eliminar adjuntos, luego la tarea (como lo hace el service)
            adjuntoRepository.deleteByTareaId(tarea.getId());
            tareaRepository.deleteById(tarea.getId());
            entityManager.flush();

            assertThat(adjuntoRepository.findById(adjuntoId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByTareaId()")
    class DeleteByTareaIdTests {

        @Test
        @DisplayName("Debería eliminar todos los adjuntos de una tarea")
        void deberiaEliminarTodosLosAdjuntos() {
            crearAdjunto("archivo1.pdf", "/uploads/uuid-1.pdf");
            crearAdjunto("archivo2.pdf", "/uploads/uuid-2.pdf");
            entityManager.flush();

            adjuntoRepository.deleteByTareaId(tarea.getId());
            entityManager.flush();

            List<Adjunto> resultado = adjuntoRepository.findByTareaId(tarea.getId());
            assertThat(resultado).isEmpty();
        }
    }

    private Adjunto crearAdjunto(String nombre, String path) {
        return crearAdjuntoParaTarea(nombre, path, tarea);
    }

    private Adjunto crearAdjuntoParaTarea(String nombre, String path, Tarea tarea) {
        Adjunto adjunto = Adjunto.builder()
                .nombre(nombre)
                .path(path)
                .mimeType("application/pdf")
                .sizeBytes(1024L)
                .tarea(tarea)
                .build();
        return adjuntoRepository.save(adjunto);
    }
}