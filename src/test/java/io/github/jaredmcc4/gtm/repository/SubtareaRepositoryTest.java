package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Subtarea;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("SubtareaRepository - Integration Tests")
class SubtareaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubtareaRepository subtareaRepository;

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
        @DisplayName("Debería encontrar subtareas de la tarea")
        void deberiaEncontrarSubtareas() {
            crearSubtarea("Subtarea 1", false);
            crearSubtarea("Subtarea 2", true);
            crearSubtarea("Subtarea 3", false);
            entityManager.flush();

            List<Subtarea> resultado = subtareaRepository.findByTareaId(tarea.getId());

            assertThat(resultado).hasSize(3);
            assertThat(resultado).extracting("titulo")
                    .containsExactlyInAnyOrder("Subtarea 1", "Subtarea 2", "Subtarea 3");
        }

        @Test
        @DisplayName("Debería retornar lista vacía si no hay subtareas")
        void deberiaRetornarListaVacia() {
            List<Subtarea> resultado = subtareaRepository.findByTareaId(tarea.getId());

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("No debería retornar subtareas de otras tareas")
        void noDeberiaRetornarSubtareasDeOtrasTareas() {
            Tarea otraTarea = Tarea.builder()
                    .titulo("Otra Tarea")
                    .descripcion("Otra descripción")
                    .estado(Tarea.EstadoTarea.PENDIENTE)
                    .prioridad(Tarea.Prioridad.BAJA)
                    .usuario(usuario)
                    .build();
            otraTarea = tareaRepository.save(otraTarea);

            crearSubtarea("Mi Subtarea", false);
            crearSubtareaParaTarea("Subtarea Ajena", false, otraTarea);
            entityManager.flush();

            List<Subtarea> resultado = subtareaRepository.findByTareaId(tarea.getId());

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getTitulo()).isEqualTo("Mi Subtarea");
        }
    }

    @Nested
    @DisplayName("Operaciones de cascada")
    class OperacionesCascadaTests {

        @Test
        @DisplayName("Debería eliminar subtareas al eliminar tarea")
        void deberiaEliminarSubtareasAlEliminarTarea() {
            Subtarea subtarea = crearSubtarea("Subtarea", false);
            Long subtareaId = subtarea.getId();

            entityManager.flush();
            entityManager.clear();

            tareaRepository.deleteById(tarea.getId());
            entityManager.flush();

            assertThat(subtareaRepository.findById(subtareaId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByTareaId()")
    class DeleteByTareaIdTests {

        @Test
        @DisplayName("Debería eliminar todas las subtareas de una tarea")
        void deberiaEliminarTodasLasSubtareas() {
            crearSubtarea("Sub 1", false);
            crearSubtarea("Sub 2", true);
            crearSubtarea("Sub 3", false);
            entityManager.flush();

            subtareaRepository.deleteByTareaId(tarea.getId());
            entityManager.flush();

            List<Subtarea> resultado = subtareaRepository.findByTareaId(tarea.getId());
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("Operaciones CRUD")
    class OperacionesCrudTests {

        @Test
        @DisplayName("Debería guardar y recuperar subtarea")
        void deberiaGuardarYRecuperarSubtarea() {
            Subtarea subtarea = crearSubtarea("Nueva Subtarea", false);
            entityManager.flush();
            entityManager.clear();

            Optional<Subtarea> recuperada = subtareaRepository.findById(subtarea.getId());

            assertThat(recuperada).isPresent();
            assertThat(recuperada.get().getTitulo()).isEqualTo("Nueva Subtarea");
            assertThat(recuperada.get().getCompletada()).isFalse();
        }

        @Test
        @DisplayName("Debería actualizar estado de completada")
        void deberiaActualizarEstadoCompletada() {
            Subtarea subtarea = crearSubtarea("Subtarea", false);
            entityManager.flush();

            subtarea.setCompletada(true);
            subtareaRepository.save(subtarea);
            entityManager.flush();
            entityManager.clear();

            Optional<Subtarea> actualizada = subtareaRepository.findById(subtarea.getId());

            assertThat(actualizada).isPresent();
            assertThat(actualizada.get().getCompletada()).isTrue();
        }

        @Test
        @DisplayName("Debería actualizar título")
        void deberiaActualizarTitulo() {
            Subtarea subtarea = crearSubtarea("Título Original", false);
            entityManager.flush();

            subtarea.setTitulo("Título Actualizado");
            subtareaRepository.save(subtarea);
            entityManager.flush();
            entityManager.clear();

            Optional<Subtarea> actualizada = subtareaRepository.findById(subtarea.getId());

            assertThat(actualizada).isPresent();
            assertThat(actualizada.get().getTitulo()).isEqualTo("Título Actualizado");
        }

        @Test
        @DisplayName("Debería eliminar subtarea correctamente")
        void deberiaEliminarSubtarea() {
            Subtarea subtarea = crearSubtarea("Eliminar", false);
            Long subtareaId = subtarea.getId();
            entityManager.flush();

            subtareaRepository.deleteById(subtareaId);
            entityManager.flush();

            Optional<Subtarea> eliminada = subtareaRepository.findById(subtareaId);

            assertThat(eliminada).isEmpty();
        }
    }

    private Subtarea crearSubtarea(String titulo, boolean completada) {
        return crearSubtareaParaTarea(titulo, completada, tarea);
    }

    private Subtarea crearSubtareaParaTarea(String titulo, boolean completada, Tarea tarea) {
        Subtarea subtarea = Subtarea.builder()
                .titulo(titulo)
                .completada(completada)
                .tarea(tarea)
                .build();
        return subtareaRepository.save(subtarea);
    }
}