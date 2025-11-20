package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.domain.Tarea;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.dto.response.PageResponse;
import io.github.jaredmcc4.gtm.dto.tarea.TareaDto;
import io.github.jaredmcc4.gtm.mapper.TareaMapper;
import io.github.jaredmcc4.gtm.services.TareaService;
import io.github.jaredmcc4.gtm.services.UsuarioService;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TareaController - Resolución de usuario")
class TareaControllerResolverTest {

    @Mock
    private TareaService tareaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private TareaMapper tareaMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TareaController tareaController;

    private Page<Tarea> page;

    @BeforeEach
    void setUp() {
        Tarea tarea = Tarea.builder()
                .id(1L)
                .usuario(Usuario.builder().id(9L).email("user@test.com").contrasenaHash("hash").build())
                .titulo("Demo")
                .build();
        page = new PageImpl<>(List.of(tarea), PageRequest.of(0, 10), 1);
        when(tareaMapper.toDto(any(Tarea.class))).thenReturn(TareaDto.builder().id(1L).titulo("Demo").build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe resolver usuario desde el SecurityContext cuando no hay Jwt directo")
    void deberiaResolverDesdeSecurityContext() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("usuarioId", 9L)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        when(jwtUtil.extraerUsuarioId(jwt.getTokenValue())).thenReturn(9L);
        when(tareaService.obtenerTareasPorUsuarioId(eq(9L), any())).thenReturn(page);

        ApiResponse<PageResponse<TareaDto>> respuesta = tareaController
                .obtenerTareas(null, 0, 10, "createdAt", "DESC", null, null)
                .getBody();

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getData().getContent()).hasSize(1);
        verify(tareaService).obtenerTareasPorUsuarioId(eq(9L), any());
    }

    @Test
    @DisplayName("buscarTareas debe delegar en el servicio con los parámetros indicados")
    void deberiaBuscarTareasPorTexto() {
        Jwt jwt = Jwt.withTokenValue("token-buscar")
                .header("alg", "none")
                .claim("usuarioId", 3L)
                .build();
        when(jwtUtil.extraerUsuarioId(jwt.getTokenValue())).thenReturn(3L);
        when(tareaService.buscarTareasPorTexto(eq(3L), eq("bug"), any()))
                .thenReturn(page);

        ApiResponse<PageResponse<TareaDto>> respuesta = tareaController
                .buscarTareas(jwt, "bug", 0, 5)
                .getBody();

        assertThat(respuesta).isNotNull();
        verify(tareaService).buscarTareasPorTexto(eq(3L), eq("bug"), any());
    }
}
