package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("TareaRepository - Integration Tests")
class TareaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EtiquetaRepository etiquetaRepository;

    private Usuario usuario;
    private Pageable pageable;

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
        entityManager.flush();

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("findByUsuarioId()")
    class FindByUsuarioIdTests {

        @Test
        @DisplayName("Debería encontrar las tareas del usuario")
        void deberiaEncontrarTareasDelUsuario() {

            crearTarea("Tarea 1", usuario);
            crearTarea("Tarea 2", usuario);
            entityManager.flush();

            Page<Tarea> resultado = tareaRepository.findByUsuarioId(usuario.getId(), pageable);

            assertThat(resultado.getContent()).hasSize(2);
            assertThat(resultado.getContent())
                    .extracting("titulo")
                    .containsExactlyInAnyOrder("Tarea 1", "Tarea 2");
        }

        @Test
        @DisplayName("No debería retornar tareas de otros usuarios")
        void noDeberiaRetornarTareasDeOtrosUsuarios() {

            Usuario otroUsuario = Usuario.builder()
                    .email("otro@example.com")
                    .contrasenaHash("$2a$12$hash")
                    .nombreUsuario("Otro Usuario")
                    .activo(true)
                    .build();
            otroUsuario = usuarioRepository.save(otroUsuario);

            crearTarea("Tarea Usuario 1", usuario);
            crearTarea("Tarea Otro Usuario", otroUsuario);
            entityManager.flush();

            Page<Tarea> resultado = tareaRepository.findByUsuarioId(usuario.getId(), pageable);

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Tarea Usuario 1");
        }
    }

    @Nested
    @DisplayName("findByIdAndUsuarioId()")
    class FindByIdAndUsuarioIdTests {

        @Test
        @DisplayName("Debería encontrar tarea por ID y usuario")
        void deberiaEncontrarTareaPorIdYUsuario() {

            Tarea tarea = crearTarea("Mi Tarea", usuario);
            entityManager.flush();

            Optional<Tarea> resultado = tareaRepository.findByIdAndUsuarioId(
                    tarea.getId(),
                    usuario.getId()
            );

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getTitulo()).isEqualTo("Mi Tarea");
        }

        @Test
        @DisplayName("No debería encontrar tareas de otro usuario")
        void noDeberiaEncontrarTareaDeOtroUsuario() {

            Usuario otroUsuario = Usuario.builder()
                    .email("otro@example.com")
                    .contrasenaHash("$2a$12$hash")
                    .nombreUsuario("Otro Usuario")
                    .activo(true)
                    .build();
            otroUsuario = usuarioRepository.save(otroUsuario);

            Tarea tarea = crearTarea("Tarea Otro", otroUsuario);
            entityManager.flush();

            Optional<Tarea> resultado = tareaRepository.findByIdAndUsuarioId(
                    tarea.getId(),
                    usuario.getId()
            );

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByFilters()")
    class FindByFiltersTests {

        @Test
        @DisplayName("Debería filtrar por estado")
        void deberiaFiltrarPorEstado() {

            crearTareaConEstado("Tarea Pendiente", Tarea.EstadoTarea.PENDIENTE);
            crearTareaConEstado("Tarea Completada", Tarea.EstadoTarea.COMPLETADA);
            entityManager.flush();

            Page<Tarea> resultado = tareaRepository.findByFilters(
                    usuario.getId(),
                    Tarea.EstadoTarea.PENDIENTE,
                    null,
                    null,
                    pageable
            );

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getEstado())
                    .isEqualTo(Tarea.EstadoTarea.PENDIENTE);
        }

        @Test
        @DisplayName("Debería filtrar por prioridad")
        void deberiaFiltrarPorPrioridad() {

            crearTareaConPrioridad("Tarea Alta", Tarea.Prioridad.ALTA);
            crearTareaConPrioridad("Tarea Baja", Tarea.Prioridad.BAJA);
            entityManager.flush();

            Page<Tarea> resultado = tareaRepository.findByFilters(
                    usuario.getId(),
                    null,
                    Tarea.Prioridad.ALTA,
                    null,
                    pageable
            );

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getPrioridad())
                    .isEqualTo(Tarea.Prioridad.ALTA);
        }

        @Test
        @DisplayName("Debería filtrar por etiqueta")
        void deberiaFiltrarPorEtiqueta() {

            Etiqueta etiqueta1 = crearEtiqueta("Trabajo");
            Etiqueta etiqueta2 = crearEtiqueta("Personal");

            Tarea tarea1 = crearTarea("Tarea Trabajo", usuario);
            tarea1.setEtiquetas(Set.of(etiqueta1));
            tareaRepository.save(tarea1);

            Tarea tarea2 = crearTarea("Tarea Personal", usuario);
            tarea2.setEtiquetas(Set.of(etiqueta2));
            tareaRepository.save(tarea2);

            entityManager.flush();

            Page<Tarea> resultado = tareaRepository.findByUsuarioIdAndEtiquetaId(
                    usuario.getId(),
                    etiqueta1.getId(),
                    pageable
            );

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Tarea Trabajo");
        }

        @Test
        @DisplayName("Debería combinar múltiples filtros")
        void deberiaCombinarMultiplesFiltros() {

            Etiqueta etiqueta = crearEtiqueta("Urgente");

            crearTareaCompleta("Tarea 1",
                    Tarea.EstadoTarea.PENDIENTE,
                    Tarea.Prioridad.ALTA,
                    etiqueta);

            crearTareaCompleta("Tarea 2",
                    Tarea.EstadoTarea.PENDIENTE,
                    Tarea.Prioridad.BAJA,
                    etiqueta);

            entityManager.flush();

            Page<Tarea> resultado = tareaRepository.findByFilters(
                    usuario.getId(),
                    Tarea.EstadoTarea.PENDIENTE,
                    Tarea.Prioridad.ALTA,
                    null,
                    pageable
            );

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Tarea 1");
        }
    }

    @Nested
    @DisplayName("searchByTexto()")
    class SearchByTextoTests {

        @Test
        @DisplayName("Debería buscar en el título")
        void deberiaBuscarEnTitulo() {

            crearTarea("Proyecto importante", usuario);
            crearTarea("Tarea secundaria", usuario);
            entityManager.flush();

            Page<Tarea> resultado = tareaRepository.searchByTexto(
                    usuario.getId(),
                    "proyecto",
                    pageable
            );

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getTitulo()).contains("Proyecto");
        }

        @Test
        @DisplayName("Debería buscar en la descripción")
        void deberiaBuscarEnDescripcion() {

            Tarea tarea = crearTarea("Tarea 1", usuario);
            tarea.setDescripcion("Descripción con palabra clave importante");
            tareaRepository.save(tarea);
            entityManager.flush();

            Page<Tarea> resultado = tareaRepository.searchByTexto(
                    usuario.getId(),
                    "clave",
                    pageable
            );

            assertThat(resultado.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Búsqueda debería ser case-insensitive")
        void busquedaDeberiaSeCaseInsensitive() {

            crearTarea("TAREA EN MAYÚSCULAS", usuario);
            entityManager.flush();

            Page<Tarea> resultado = tareaRepository.searchByTexto(
                    usuario.getId(),
                    "mayúsculas",
                    pageable
            );

            assertThat(resultado.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findProximasVencer()")
    class FindProximasVencerTests {

        @Test
        @DisplayName("Debería encontrar tareas próximas a vencer")
        void deberiaEncontrarTareasProximasVencer() {

            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime fin = ahora.plusDays(7);

            Tarea tarea1 = crearTarea("Vence en 3 días", usuario);
            tarea1.setFechaVencimiento(ahora.plusDays(3));
            tarea1.setEstado(Tarea.EstadoTarea.PENDIENTE);
            tareaRepository.save(tarea1);

            Tarea tarea2 = crearTarea("Vence en 10 días", usuario);
            tarea2.setFechaVencimiento(ahora.plusDays(10));
            tarea2.setEstado(Tarea.EstadoTarea.PENDIENTE);
            tareaRepository.save(tarea2);

            entityManager.flush();

            List<Tarea> resultado = tareaRepository.findProximasVencer(
                    usuario.getId(),
                    ahora,
                    fin
            );

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getTitulo()).isEqualTo("Vence en 3 días");
        }

        @Test
        @DisplayName("No debería incluir tareas completadas")
        void noDeberiaIncluirTareasCompletadas() {

            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime fin = ahora.plusDays(7);

            Tarea tareaCompletada = crearTarea("Completada", usuario);
            tareaCompletada.setFechaVencimiento(ahora.plusDays(3));
            tareaCompletada.setEstado(Tarea.EstadoTarea.COMPLETADA);
            tareaRepository.save(tareaCompletada);

            entityManager.flush();

            List<Tarea> resultado = tareaRepository.findProximasVencer(
                    usuario.getId(),
                    ahora,
                    fin
            );

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("Operaciones de Cascada")
    class OperacionesCascadaTests {

        @Test
        @DisplayName("Debería eliminar tarea con sus subtareas")
        void deberiaEliminarTareaConSubtareas() {

            Tarea tarea = crearTarea("Tarea con subtareas", usuario);
            entityManager.flush();
            Long tareaId = tarea.getId();

            tareaRepository.delete(tarea);
            entityManager.flush();

            Optional<Tarea> eliminada = tareaRepository.findById(tareaId);
            assertThat(eliminada).isEmpty();
        }

        @Test
        @DisplayName("Debería mantener etiquetas al eliminar tarea")
        void deberiaMantenerEtiquetasAlEliminarTarea() {

            Etiqueta etiqueta = crearEtiqueta("Mantener");
            Tarea tarea = crearTarea("Tarea con etiqueta", usuario);
            tarea.setEtiquetas(Set.of(etiqueta));
            tareaRepository.save(tarea);
            entityManager.flush();
            Long etiquetaId = etiqueta.getId();

            tareaRepository.delete(tarea);
            entityManager.flush();

            Optional<Etiqueta> etiquetaExiste = etiquetaRepository.findById(etiquetaId);
            assertThat(etiquetaExiste).isPresent();
        }
    }

    private Tarea crearTarea(String titulo, Usuario usuario) {
        Tarea tarea = Tarea.builder()
                .titulo(titulo)
                .descripcion("Descripción de " + titulo)
                .prioridad(Tarea.Prioridad.MEDIA)
                .estado(Tarea.EstadoTarea.PENDIENTE)
                .fechaVencimiento(LocalDateTime.now().plusDays(7))
                .usuario(usuario)
                .build();
        return tareaRepository.save(tarea);
    }

    private Tarea crearTareaConEstado(String titulo, Tarea.EstadoTarea estado) {
        Tarea tarea = crearTarea(titulo, usuario);
        tarea.setEstado(estado);
        return tareaRepository.save(tarea);
    }

    private Tarea crearTareaConPrioridad(String titulo, Tarea.Prioridad prioridad) {
        Tarea tarea = crearTarea(titulo, usuario);
        tarea.setPrioridad(prioridad);
        return tareaRepository.save(tarea);
    }

    private Tarea crearTareaCompleta(String titulo, Tarea.EstadoTarea estado,
                                     Tarea.Prioridad prioridad, Etiqueta etiqueta) {
        Tarea tarea = crearTarea(titulo, usuario);
        tarea.setEstado(estado);
        tarea.setPrioridad(prioridad);
        tarea.setEtiquetas(Set.of(etiqueta));
        return tareaRepository.save(tarea);
    }

    private Etiqueta crearEtiqueta(String nombre) {
        Etiqueta etiqueta = Etiqueta.builder()
                .nombre(nombre)
                .colorHex("#FF5733")
                .usuario(usuario)
                .build();
        return etiquetaRepository.save(etiqueta);
    }
}