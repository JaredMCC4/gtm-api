package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Subtarea;
import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.subtarea.SubtareaDto;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.mapper.SubtareaMapper;
import io.github.jaredmcc4.gtm.services.SubtareaService;
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

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SubtareaController - Resolución de usuario")
class SubtareaControllerResolverTest {

    @Mock
    private SubtareaService subtareaService;

    @Mock
    private SubtareaMapper subtareaMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private SubtareaController subtareaController;

    private Jwt jwt;

    @BeforeEach
    void setUp() {
        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("usuarioId", 8L)
                .build();

        Subtarea subtarea = Subtarea.builder()
                .id(1L)
                .titulo("Subtarea")
                .tarea(Tarea.builder().id(2L).usuario(Usuario.builder().id(8L).email("user@test.com").contrasenaHash("hash").build()).build())
                .build();

        when(subtareaService.mostrarSubtareas(2L, 8L)).thenReturn(List.of(subtarea));
        when(subtareaMapper.toDto(subtarea)).thenReturn(SubtareaDto.builder().id(1L).titulo("Subtarea").build());
    }

    @Test
    @DisplayName("Debe resolver usuario utilizando el Jwt principal")
    void deberiaResolverUsuarioConJwt() {
        ApiResponse<List<SubtareaDto>> respuesta = subtareaController
                .obtenerSubtareasPorTarea(jwt, null, 2L)
                .getBody();

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getData()).hasSize(1);
        verify(subtareaService).mostrarSubtareas(2L, 8L);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Debe lanzar Unauthorized cuando no hay encabezado ni Jwt")
    void deberiaLanzarUnauthorizedSinCredenciales() {
        assertThatThrownBy(() -> subtareaController.eliminarSubtarea(null, null, 1L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Debe resolver usuario usando Authorization header")
    void deberiaResolverConHeader() {
        when(jwtUtil.extraerUsuarioId("token-header")).thenReturn(8L);
        when(subtareaService.mostrarSubtareas(2L, 8L)).thenReturn(List.of());

        subtareaController.obtenerSubtareasPorTarea(null, "Bearer token-header", 2L);

        verify(jwtUtil).extraerUsuarioId("token-header");
        verify(subtareaService).mostrarSubtareas(2L, 8L);
    }
}
