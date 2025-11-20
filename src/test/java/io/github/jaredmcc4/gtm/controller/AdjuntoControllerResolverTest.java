package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Adjunto;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.adjunto.AdjuntoDto;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.AdjuntoMapper;
import io.github.jaredmcc4.gtm.services.AdjuntoService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdjuntoController - Resolución de usuario")
class AdjuntoControllerResolverTest {

    @Mock
    private AdjuntoService adjuntoService;

    @Mock
    private AdjuntoMapper adjuntoMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AdjuntoController adjuntoController;

    private Jwt jwt;

    @BeforeEach
    void setUp() {
        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("usuarioId", 4L)
                .build();

        Adjunto adjunto = Adjunto.builder()
                .id(1L)
                .nombre("archivo.pdf")
                .tarea(Tarea.builder().id(2L).usuario(Usuario.builder().id(4L).email("user@test.com").contrasenaHash("hash").build()).build())
                .build();

        when(adjuntoService.mostrarAdjuntos(2L, 4L)).thenReturn(List.of(adjunto));
        when(adjuntoMapper.toDto(adjunto)).thenReturn(AdjuntoDto.builder().id(1L).nombre("archivo.pdf").build());
    }

    @Test
    @DisplayName("Debe obtener adjuntos utilizando el Jwt principal")
    void deberiaObtenerAdjuntosConJwt() {
        ApiResponse<List<AdjuntoDto>> respuesta = adjuntoController
                .obtenerAdjuntosPorTarea(2L, jwt, null)
                .getBody();

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getData()).hasSize(1);
        verify(adjuntoService).mostrarAdjuntos(2L, 4L);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Debe lanzar Unauthorized cuando faltan credenciales")
    void deberiaLanzarUnauthorizedSinCredenciales() {
        assertThatThrownBy(() -> adjuntoController.eliminarAdjunto(1L, null, null))
                .isInstanceOf(UnauthorizedException.class);
    }
}
