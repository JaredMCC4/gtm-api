package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.builders.UsuarioTestBuilder;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Usuario Service - Unit Tests")
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioBase;

    @BeforeEach
    void setUp() {
        usuarioBase = UsuarioTestBuilder.unUsuario()
                .conId(1L)
                .conEmail("test@test.com")
                .conNombreUsuario("Test_User")
                .build();
    }

    @Nested
    @DisplayName("obtenerUsuarioPorId()")
    class ObtenerUsuarioPorIdTests {

        @Test
        @DisplayName("Debería retornar un usuario en caso de que exista")
        void deberiaRetornarUsuarioCuandoExiste() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            Usuario resultado = usuarioService.obtenerUsuarioPorId(1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getEmail()).isEqualTo("test@test.com");
            verify(usuarioRepository).findById(1L);
        }

        @Test
        @DisplayName("Debería lanzar ResourceNotFoundException en caso de que el usuario no exista")
        void deberiaLanzarExcepcionCuandoUsuarioNoExiste() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> usuarioService.obtenerUsuarioPorId(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");

            verify(usuarioRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("actualizarUsuario()")
    class ActualizarUsuarioTests {

        @Test
        @DisplayName("Debería actualizar nombre de usuario correctamente")
        void deberiaActualizarNombreUsuario() {
            Usuario usuarioActualizado = Usuario.builder()
                    .nombreUsuario("Nuevo Nombre")
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioBase);
            Usuario resultado = usuarioService.actualizarUsuario(1L, usuarioActualizado);

            assertThat(resultado.getNombreUsuario()).isEqualTo("Nuevo Nombre");
            verify(usuarioRepository).save(argThat(usuario ->
                    usuario.getNombreUsuario().equals("Nuevo Nombre")
            ));
        }

        @Test
        @DisplayName("Debería rechazar un nombre de usuario mayor a 120 carácteres")
        void deberiaRechazarNombreLargo() {
            String nombreLargo = "a".repeat(121);
            Usuario usuarioActualizado = Usuario.builder()
                    .nombreUsuario(nombreLargo)
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));

            assertThatThrownBy(() -> usuarioService.actualizarUsuario(1L, usuarioActualizado))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no puede ser mayor a 120 caracteres");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería actualizar la zona horaria correctamente")
        void deberiaActualizarZonaHoraria() {
            Usuario usuarioActualizado = Usuario.builder()
                    .zonaHoraria("America/New_York")
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioBase);

            Usuario resultado = usuarioService.actualizarUsuario(1L, usuarioActualizado);

            assertThat(resultado.getZonaHoraria()).isEqualTo("America/New_York");
        }

        @Test
        @DisplayName("No debería poder actualizar cuando el nombre es vacío o blank")
        void noDeberiaActualizarNombreVacio() {
            Usuario usuarioActualizado = Usuario.builder()
                    .nombreUsuario("   ")
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioBase);

            usuarioService.actualizarUsuario(1L, usuarioActualizado);

            verify(usuarioRepository).save(argThat(usuario ->
                    usuario.getNombreUsuario().equals("Test_User")
            ));
        }
    }

    @Nested
    @DisplayName("cambiarPassword()")
    class CambiarPasswordTests {

        @Test
        @DisplayName("Debería dejar cambiar la contraseña cuando la contraseña actual sea correcta")
        void deberiaCambiarPasswordCuandoEsCorrecta() {
            String passwordActual = "Password123,";
            String passwordNueva = "password456A!";
            String hashNuevo = "$2b$12$14nGIS/IcmCKAKJpyfpjquE/Su.TdACxrZ2wptmvPjbQ1c8iFX84m";

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            when(passwordEncoder.matches(passwordActual, usuarioBase.getContrasenaHash())).thenReturn(true);
            when(passwordEncoder.matches(passwordNueva, usuarioBase.getContrasenaHash())).thenReturn(false);
            when(passwordEncoder.encode(passwordNueva)).thenReturn(hashNuevo);
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioBase);

            usuarioService.cambiarPassword(1L, passwordActual, passwordNueva);

            verify(passwordEncoder).matches(passwordActual, usuarioBase.getContrasenaHash());
            verify(passwordEncoder).encode(passwordNueva);
            verify(usuarioRepository).save(argThat(usuario ->
                    usuario.getContrasenaHash().equals(hashNuevo)
            ));
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando la contraseña actual sea incorrecta")
        void deberiaLanzarExcepcionCuandoPasswordIncorrecta() {

            String passwordActual = "passwordIncorr.ecta2094954";
            String passwordNueva = "nuevaPassword1!3";

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            when(passwordEncoder.matches(passwordActual, usuarioBase.getContrasenaHash())).thenReturn(false);
            assertThatThrownBy(() -> usuarioService.cambiarPassword(1L, passwordActual, passwordNueva))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("La contraseña actual es incorrecta");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar la contraseña nueva si es la misma que la actual")
        void deberiaRechazarPasswordNuevaIgual() {
            String password = "Password123,";

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            when(passwordEncoder.matches(password, usuarioBase.getContrasenaHash())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> usuarioService.cambiarPassword(1L, password, password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("debe ser diferente de la actual");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar contraseña nueva menor a 8 caracteres")
        void deberiaRechazarPasswordMuyCorta() {

            String passwordActual = "Password123,";
            String passwordNueva = "short";
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            when(passwordEncoder.matches(passwordActual, usuarioBase.getContrasenaHash())).thenReturn(true);
            when(passwordEncoder.matches(passwordNueva, usuarioBase.getContrasenaHash())).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.cambiarPassword(1L, passwordActual, passwordNueva))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Al menos 8 carácteres");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debería rechazar una contraseña nueva si es null")
        void deberiaRechazarPasswordNull() {
            String passwordActual = "Password123,";

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
            when(passwordEncoder.matches(passwordActual, usuarioBase.getContrasenaHash())).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.cambiarPassword(1L, passwordActual, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Al menos 8 carácteres");

            verify(usuarioRepository, never()).save(any());
        }
    }
}