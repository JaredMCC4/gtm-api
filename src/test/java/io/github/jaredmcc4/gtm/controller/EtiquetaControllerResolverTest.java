package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Etiqueta;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.etiqueta.EtiquetaDto;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.EtiquetaMapper;
import io.github.jaredmcc4.gtm.services.EtiquetaService;
import io.github.jaredmcc4.gtm.services.UsuarioService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EtiquetaController - Resolución de usuario")
class EtiquetaControllerResolverTest {

    @Mock
    private EtiquetaService etiquetaService;

    @Mock
    private EtiquetaMapper etiquetaMapper;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private EtiquetaController etiquetaController;

    private Jwt jwt;

    @BeforeEach
    void setUp() {
        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("usuarioId", 3L)
                .build();

        Etiqueta etiqueta = Etiqueta.builder()
                .id(1L)
                .nombre("Trabajo")
                .colorHex("#FFFFFF")
                .usuario(Usuario.builder().id(3L).email("user@test.com").contrasenaHash("hash").build())
                .build();

        when(etiquetaService.obtenerEtiquetaPorIdYUsuarioId(1L, 3L)).thenReturn(etiqueta);
        when(etiquetaMapper.toDto(etiqueta)).thenReturn(EtiquetaDto.builder()
                .id(1L)
                .nombre("Trabajo")
                .colorHex("#FFFFFF")
                .build());
    }

    @Test
    @DisplayName("Debe obtener la etiqueta usando el Jwt principal")
    void deberiaObtenerEtiquetaPorIdUsandoJwt() {
        ApiResponse<EtiquetaDto> respuesta = etiquetaController
                .obtenerEtiquetaPorId(jwt, null, 1L)
                .getBody();

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getData().getId()).isEqualTo(1L);
        verify(etiquetaService).obtenerEtiquetaPorIdYUsuarioId(1L, 3L);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Debe lanzar Unauthorized cuando no hay credenciales")
    void deberiaLanzarUnauthorizedSinCredenciales() {
        assertThatThrownBy(() -> etiquetaController.obtenerEtiquetas(null, null))
                .isInstanceOf(UnauthorizedException.class);
    }
}
