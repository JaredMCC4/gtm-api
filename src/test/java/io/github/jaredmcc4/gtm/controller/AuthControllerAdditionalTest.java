package io.github.jaredmcc4.gtm.controller;

import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import io.github.jaredmcc4.gtm.mapper.UsuarioMapper;
import io.github.jaredmcc4.gtm.services.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Casos adicionales")
class AuthControllerAdditionalTest {

    @Mock
    private AuthService authService;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("validarToken debe delegar en el AuthService")
    void validarTokenDebeDelegar() {
        ApiResponse<Void> respuesta = authController.validarToken("jwt-token").getBody();

        assertThat(respuesta).isNotNull();
        verify(authService).validarToken("jwt-token");
    }
}
