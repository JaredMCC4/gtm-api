package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.usuario.ActualizarUsuarioRequest;
import io.github.jaredmcc4.gtm.dto.usuario.UsuarioDto;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.UsuarioMapper;
import io.github.jaredmcc4.gtm.services.UsuarioService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UsuarioController - Resolución de usuario")
class UsuarioControllerResolverTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioController usuarioController;

    private Jwt buildJwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("usuarioId", 5L)
                .build();
    }

    @BeforeEach
    void setUp() {
        when(usuarioService.obtenerUsuarioPorId(5L)).thenReturn(Usuario.builder().id(5L).email("user@test.com").contrasenaHash("hash").build());
        when(usuarioMapper.toDto(any(Usuario.class))).thenReturn(UsuarioDto.builder().id(5L).email("user@test.com").build());
    }

    @Test
    @DisplayName("Debe resolver el usuario a partir del Jwt principal")
    void deberiaResolverUsuarioDesdeJwt() {
        ApiResponse<UsuarioDto> respuesta = usuarioController
                .obtenerPerfil(buildJwt(), null)
                .getBody();

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getData().getId()).isEqualTo(5L);
        verify(usuarioService).obtenerUsuarioPorId(5L);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Debe lanzar Unauthorized cuando no hay credenciales")
    void deberiaLanzarUnauthorizedSinCredenciales() {
        ActualizarUsuarioRequest request = new ActualizarUsuarioRequest("Nombre", "America/Costa_Rica");

        assertThatThrownBy(() -> usuarioController.actualizarPerfil(null, null, request))
                .isInstanceOf(UnauthorizedException.class);
    }
}
