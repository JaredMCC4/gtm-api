package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Rol;
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
@TestPropertySource(locations = "classpath:application.properties")
@DisplayName("EtiquetaRepository - Integration Tests")
class EtiquetaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EtiquetaRepository etiquetaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Usuario usuario;

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
    }

    @Nested
    @DisplayName("findByUsuarioId()")
    class FindByUsuarioIdTests {

        @Test
        @DisplayName("Debería encontrar etiquetas del usuario")
        void deberiaEncontrarEtiquetas() {
            crearEtiqueta("Trabajo", "#FF0000");
            crearEtiqueta("Personal", "#00FF00");
            crearEtiqueta("Urgente", "#0000FF");
            entityManager.flush();

            List<Etiqueta> resultado = etiquetaRepository.findByUsuarioId(usuario.getId());

            assertThat(resultado).hasSize(3);
            assertThat(resultado).extracting("nombre")
                    .containsExactlyInAnyOrder("Trabajo", "Personal", "Urgente");
        }

        @Test
        @DisplayName("No debería retornar etiquetas de otros usuarios")
        void noDeberiaRetornarEtiquetasDeOtrosUsuarios() {
            Usuario otroUsuario = Usuario.builder()
                    .email("otro@example.com")
                    .contrasenaHash("$2a$12$hash")
                    .nombreUsuario("Otro Usuario")
                    .activo(true)
                    .roles(Set.of(rolRepository.findByNombreRol("USER").get()))
                    .build();
            otroUsuario = usuarioRepository.save(otroUsuario);

            crearEtiqueta("Mi Etiqueta", "#FF0000");
            crearEtiquetaParaUsuario("Etiqueta Ajena", "#00FF00", otroUsuario);
            entityManager.flush();

            List<Etiqueta> resultado = etiquetaRepository.findByUsuarioId(usuario.getId());

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Mi Etiqueta");
        }

        @Test
        @DisplayName("Debería retornar lista vacía si no hay etiquetas")
        void deberiaRetornarListaVacia() {
            List<Etiqueta> resultado = etiquetaRepository.findByUsuarioId(usuario.getId());

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUsuarioIdAndNombre()")
    class FindByUsuarioIdAndNombreTests {

        @Test
        @DisplayName("Debería encontrar etiqueta por nombre exacto")
        void deberiaEncontrarPorNombre() {
            crearEtiqueta("Trabajo", "#FF0000");
            entityManager.flush();

            Optional<Etiqueta> resultado = etiquetaRepository.findByUsuarioIdAndNombre(
                    usuario.getId(), "Trabajo"
            );

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getNombre()).isEqualTo("Trabajo");
        }

        @Test
        @DisplayName("No debería encontrar con nombre diferente")
        void noDeberiaEncontrarConNombreDiferente() {
            crearEtiqueta("Trabajo", "#FF0000");
            entityManager.flush();

            Optional<Etiqueta> resultado = etiquetaRepository.findByUsuarioIdAndNombre(
                    usuario.getId(), "Personal"
            );

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUsuarioIdAndNombre()")
    class ExistsByUsuarioIdAndNombreTests {

        @Test
        @DisplayName("Debería retornar true si existe etiqueta con ese nombre")
        void deberiaRetornarTrueSiExiste() {
            crearEtiqueta("Trabajo", "#FF0000");
            entityManager.flush();

            boolean existe = etiquetaRepository.existsByUsuarioIdAndNombre(
                    usuario.getId(), "Trabajo"
            );

            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("Debería retornar false si no existe")
        void deberiaRetornarFalseSiNoExiste() {
            boolean existe = etiquetaRepository.existsByUsuarioIdAndNombre(
                    usuario.getId(), "Inexistente"
            );

            assertThat(existe).isFalse();
        }

        @Test
        @DisplayName("Debería ser case-sensitive")
        void deberiaSerCaseSensitive() {
            crearEtiqueta("Trabajo", "#FF0000");
            entityManager.flush();

            boolean existeMayusculas = etiquetaRepository.existsByUsuarioIdAndNombre(
                    usuario.getId(), "TRABAJO"
            );

            assertThat(existeMayusculas).isFalse();
        }

        @Test
        @DisplayName("No debería encontrar etiquetas de otros usuarios")
        void noDeberiaEncontrarEtiquetasDeOtrosUsuarios() {
            Usuario otroUsuario = Usuario.builder()
                    .email("otro@example.com")
                    .contrasenaHash("$2a$12$hash")
                    .nombreUsuario("Otro Usuario")
                    .activo(true)
                    .roles(Set.of(rolRepository.findByNombreRol("USER").get()))
                    .build();
            otroUsuario = usuarioRepository.save(otroUsuario);

            crearEtiquetaParaUsuario("Trabajo", "#FF0000", otroUsuario);
            entityManager.flush();

            boolean existe = etiquetaRepository.existsByUsuarioIdAndNombre(
                    usuario.getId(), "Trabajo"
            );

            assertThat(existe).isFalse();
        }
    }

    @Nested
    @DisplayName("Operaciones CRUD")
    class OperacionesCrudTests {

        @Test
        @DisplayName("Debería guardar y recuperar etiqueta")
        void deberiaGuardarYRecuperarEtiqueta() {
            Etiqueta etiqueta = crearEtiqueta("Nueva", "#123456");
            entityManager.flush();
            entityManager.clear();

            Optional<Etiqueta> recuperada = etiquetaRepository.findById(etiqueta.getId());

            assertThat(recuperada).isPresent();
            assertThat(recuperada.get().getNombre()).isEqualTo("Nueva");
            assertThat(recuperada.get().getColorHex()).isEqualTo("#123456");
        }

        @Test
        @DisplayName("Debería actualizar etiqueta correctamente")
        void deberiaActualizarEtiqueta() {
            Etiqueta etiqueta = crearEtiqueta("Original", "#FF0000");
            entityManager.flush();

            etiqueta.setNombre("Actualizado");
            etiqueta.setColorHex("#00FF00");
            etiquetaRepository.save(etiqueta);
            entityManager.flush();
            entityManager.clear();

            Optional<Etiqueta> actualizada = etiquetaRepository.findById(etiqueta.getId());

            assertThat(actualizada).isPresent();
            assertThat(actualizada.get().getNombre()).isEqualTo("Actualizado");
            assertThat(actualizada.get().getColorHex()).isEqualTo("#00FF00");
        }

        @Test
        @DisplayName("Debería eliminar etiqueta correctamente")
        void deberiaEliminarEtiqueta() {
            Etiqueta etiqueta = crearEtiqueta("Eliminar", "#FF0000");
            Long etiquetaId = etiqueta.getId();
            entityManager.flush();

            etiquetaRepository.deleteById(etiquetaId);
            entityManager.flush();

            Optional<Etiqueta> eliminada = etiquetaRepository.findById(etiquetaId);

            assertThat(eliminada).isEmpty();
        }

        @Test
        @DisplayName("Debería respetar constraint único de nombre por usuario")
        void deberiaRespetarConstraintUnicoNombre() {
            crearEtiqueta("Trabajo", "#FF0000");
            entityManager.flush();

            Etiqueta etiquetaDuplicada = Etiqueta.builder()
                    .nombre("Trabajo")
                    .colorHex("#00FF00")
                    .usuario(usuario)
                    .build();

            assertThatThrownBy(() -> {
                etiquetaRepository.save(etiquetaDuplicada);
                entityManager.flush();
            }).isInstanceOf(Exception.class);
        }
    }

    private Etiqueta crearEtiqueta(String nombre, String colorHex) {
        return crearEtiquetaParaUsuario(nombre, colorHex, usuario);
    }

    private Etiqueta crearEtiquetaParaUsuario(String nombre, String colorHex, Usuario usuario) {
        Etiqueta etiqueta = Etiqueta.builder()
                .nombre(nombre)
                .colorHex(colorHex)
                .usuario(usuario)
                .build();
        return etiquetaRepository.save(etiqueta);
    }
}