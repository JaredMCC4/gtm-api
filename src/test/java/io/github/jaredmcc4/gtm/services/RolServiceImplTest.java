package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.RolRepository;
import io.github.jaredmcc4.gtm.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RolServiceImpl - Unit Tests")
class RolServiceImplTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    private Rol rol;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        rol = Rol.builder().id(1L).nombreRol("USER").build();
        usuario = Usuario.builder().id(10L).build();
    }

    @Nested
    @DisplayName("crearRol()")
    class CrearRolTests {

        @Test
        @DisplayName("Debería crear un nuevo rol cuando no existe duplicado")
        void deberiaCrearRol() {
            when(rolRepository.existsByNombreRol("USER")).thenReturn(false);
            when(rolRepository.save(any(Rol.class))).thenReturn(rol);

            Rol resultado = rolService.crearRol(rol);

            assertThat(resultado).isEqualTo(rol);
            verify(rolRepository).save(rol);
        }

        @Test
        @DisplayName("Debe rechazar roles duplicados")
        void deberiaRechazarRolDuplicado() {
            when(rolRepository.existsByNombreRol("USER")).thenReturn(true);

            assertThatThrownBy(() -> rolService.crearRol(rol))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ya existe");

            verify(rolRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("mostrarRoles() debe retornar todos los roles")
    void deberiaListarRoles() {
        when(rolRepository.findAll()).thenReturn(List.of(rol));

        assertThat(rolService.mostrarRoles())
                .hasSize(1)
                .containsExactly(rol);
    }

    @Nested
    @DisplayName("asignarRol()")
    class AsignarRolTests {

        @Test
        @DisplayName("Debe asignar el rol cuando usuario y rol existen")
        void deberiaAsignarRol() {
            when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
            when(rolRepository.findByNombreRol("USER")).thenReturn(Optional.of(rol));

            rolService.asignarRol(10L, "USER");

            verify(usuarioRepository).save(usuario);
            assertThat(usuario.getRoles()).contains(rol);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el usuario no existe")
        void deberiaFallarSinUsuario() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rolService.asignarRol(99L, "USER"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si el rol no existe")
        void deberiaFallarSinRol() {
            when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
            when(rolRepository.findByNombreRol("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rolService.asignarRol(10L, "UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
