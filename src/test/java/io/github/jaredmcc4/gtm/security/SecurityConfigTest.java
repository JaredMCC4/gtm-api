package io.github.jaredmcc4.gtm.security;

import io.github.jaredmcc4.gtm.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@DisplayName("Security Config - Integration Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("Rutas Públicas")
    class RutasPublicasTests {

        @Test
        @DisplayName("Debería permitir acceso a /api/auth/registro sin autenticación")
        void deberiaPermitirRegistro() throws Exception {
            mockMvc.perform(post("/api/auth/registro")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotUnauthorized());
        }

        @Test
        @DisplayName("Debería permitir acceso a /api/auth/login sin autenticación")
        void deberiaPermitirLogin() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotUnauthorized());
        }

        @Test
        @DisplayName("Debería permitir acceso a Actuator health sin autenticación")
        void deberiaPermitirActuatorHealth() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debería permitir acceso a OpenAPI docs sin autenticación")
        void deberiaPermitirOpenApiDocs() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debería permitir acceso a Swagger UI sin autenticación")
        void deberiaPermitirSwaggerUi() throws Exception {
            mockMvc.perform(get("/swagger-ui.html"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("Rutas Protegidas")
    class RutasProtegidasTests {

        @Test
        @DisplayName("Debería denegar acceso a /api/tareas sin token")
        void deberiaDenegarAccesoSinToken() throws Exception {
            mockMvc.perform(get("/api/tareas"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Debería denegar acceso a /api/usuarios/perfil sin token")
        void deberiaDenegarAccesoPerfilSinToken() throws Exception {
            mockMvc.perform(get("/api/usuarios/perfil"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Debería permitir acceso con token válido")
        void deberiaPermitirAccesoConTokenValido() throws Exception {
            String token = "valid.jwt.token";
            when(jwtUtil.extraerEmail(anyString())).thenReturn("test@example.com");
            when(jwtUtil.validarToken(anyString(), anyString())).thenReturn(true);
            when(jwtUtil.extraerUsuarioId(anyString())).thenReturn(1L);
            when(jwtUtil.extraerRoles(anyString())).thenReturn(List.of("USER"));

            mockMvc.perform(get("/api/tareas")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotUnauthorized());
        }

        @Test
        @DisplayName("Debería denegar acceso con token inválido")
        void deberiaDenegarAccesoConTokenInvalido() throws Exception {

            String token = "invalid.jwt.token";
            when(jwtUtil.extraerEmail(anyString())).thenReturn("test@example.com");
            when(jwtUtil.validarToken(anyString(), anyString())).thenReturn(false);

            mockMvc.perform(get("/api/tareas")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Debería denegar acceso con token malformado")
        void deberiaDenegarAccesoConTokenMalformado() throws Exception {
            mockMvc.perform(get("/api/tareas")
                            .header("Authorization", "Bearer malformed"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Debería denegar acceso sin prefijo Bearer")
        void deberiaDenegarAccesoSinPrefijoBearer() throws Exception {
            mockMvc.perform(get("/api/tareas")
                            .header("Authorization", "just.a.token"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("CORS")
    class CorsTests {

        @Test
        @DisplayName("Debería permitir solicitudes CORS desde origen permitido")
        void deberiaPermitirCorsOrigenPermitido() throws Exception {
            mockMvc.perform(options("/api/tareas")
                            .header("Origin", "http://localhost:3000")
                            .header("Access-Control-Request-Method", "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Access-Control-Allow-Origin"));
        }
    }

    @Nested
    @DisplayName("Métodos HTTP")
    class MetodosHttpTests {

        @Test
        @DisplayName("Debería denegar POST a /api/tareas sin CSRF")
        void deberiaDenegarPostSinCsrf() throws Exception {
            mockMvc.perform(post("/api/tareas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería permitir POST a /api/tareas con CSRF")
        void deberiaPermitirPostConCsrf() throws Exception {
            mockMvc.perform(post("/api/tareas")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotForbidden());
        }
    }
}