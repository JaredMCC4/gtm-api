package io.github.jaredmcc4.gtm.repository;

import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Usuario Repository - Integration Tests")
class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Rol rolUser;

    @BeforeEach
    void setUp() {
        rolUser = rolRepository.findByNombreRol("USER")
                .orElseGet(() -> {
                    Rol rol = Rol.builder().nombreRol("USER").build();
                    return rolRepository.save(rol);
                });
    }

    @Test
    @DisplayName("Debería poder guardar y recuperar un usuario correctamente")
    void deberiaGuardarYRecuperarUsuario() {

        Usuario usuario = Usuario.builder()
                .email("test@example.com")
                .contrasenaHash("$2a$12$hashedPassword")
                .nombreUsuario("Usuario Test")
                .zonaHoraria("America/Costa_Rica")
                .activo(true)
                .roles(Set.of(rolUser))
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear();

        Optional<Usuario> recuperado = usuarioRepository.findById(guardado.getId());

        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getEmail()).isEqualTo("test@example.com");
        assertThat(recuperado.get().getNombreUsuario()).isEqualTo("Usuario Test");
        assertThat(recuperado.get().isActivo()).isTrue();
    }

    @Test
    @DisplayName("Debería poder encontrar un usuario por su email")
    void deberiaEncontrarPorEmail() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .email("find@example.com")
                .contrasenaHash("$2a$12$hashedPassword")
                .nombreUsuario("Usuario Buscar")
                .activo(true)
                .roles(Set.of(rolUser))
                .build();
        usuarioRepository.save(usuario);
        entityManager.flush();

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("find@example.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("find@example.com");
    }

    @Test
    @DisplayName("Debería verificar si existe un usuario por su email")
    void deberiaVerificarExistenciaPorEmail() {

        Usuario usuario = Usuario.builder()
                .email("exists@example.com")
                .contrasenaHash("$2a$12$hashedPassword")
                .nombreUsuario("Usuario Existe")
                .activo(true)
                .roles(Set.of(rolUser))
                .build();
        usuarioRepository.save(usuario);
        entityManager.flush();

        boolean existe = usuarioRepository.existsByEmail("exists@example.com");
        boolean noExiste = usuarioRepository.existsByEmail("noexists@example.com");

        assertThat(existe).isTrue();
        assertThat(noExiste).isFalse();
    }

    @Test
    @DisplayName("Debería cargar al usuario con sus roles usando findByEmailWithRoles")
    void deberiaCargUsuarioConRoles() {
        Rol rolUser = rolRepository.findByNombreRol("USER")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombreRol("USER").build()));

        Usuario usuario = Usuario.builder()
                .email("withroles@example.com")
                .contrasenaHash("$2a$12$hashedPassword")
                .nombreUsuario("Usuario Con Roles")
                .activo(true)
                .roles(new HashSet<>(Set.of(rolUser)))
                .build();
        usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear();

        Optional<Usuario> encontrado = usuarioRepository.findByEmailWithRoles("withroles@example.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getRoles()).isNotEmpty();
        assertThat(encontrado.get().getRoles()).hasSize(1);
        assertThat(encontrado.get().getRoles()).extracting("nombreRol").contains("USER");
    }

    @Test
    @DisplayName("Debería respetar constraint único de email")
    void deberiaRespetarConstraintUnicoEmail() {

        Usuario usuario1 = Usuario.builder()
                .email("duplicate@example.com")
                .contrasenaHash("$2a$12$hashedPassword")
                .nombreUsuario("Usuario 1")
                .activo(true)
                .roles(Set.of(rolUser))
                .build();
        usuarioRepository.save(usuario1);
        entityManager.flush();

        Usuario usuario2 = Usuario.builder()
                .email("duplicate@example.com")
                .contrasenaHash("$2a$12$hashedPassword")
                .nombreUsuario("Usuario 2")
                .activo(true)
                .roles(Set.of(rolUser))
                .build();

        assertThatThrownBy(() -> {
            usuarioRepository.save(usuario2);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Debería actualizar al usuario correctamente")
    void deberiaActualizarUsuario() {

        Usuario usuario = Usuario.builder()
                .email("update@example.com")
                .contrasenaHash("$2a$12$hashedPassword")
                .nombreUsuario("Usuario Original")
                .activo(true)
                .roles(Set.of(rolUser))
                .build();
        Usuario guardado = usuarioRepository.save(usuario);
        entityManager.flush();


        guardado.setNombreUsuario("Usuario Actualizado");
        guardado.setZonaHoraria("America/New_York");
        usuarioRepository.save(guardado);
        entityManager.flush();
        entityManager.clear();

        Optional<Usuario> actualizado = usuarioRepository.findById(guardado.getId());

        assertThat(actualizado).isPresent();
        assertThat(actualizado.get().getNombreUsuario()).isEqualTo("Usuario Actualizado");
        assertThat(actualizado.get().getZonaHoraria()).isEqualTo("America/New_York");
    }
}