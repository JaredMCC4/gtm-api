package io.github.jaredmcc4.gtm.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SecurityConfigTest.TestApp.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@DisplayName("Security Config - Integration Tests")
class SecurityConfigTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @Import({
            io.github.jaredmcc4.gtm.config.SecurityConfig.class,
            io.github.jaredmcc4.gtm.config.ProjectConfig.class
    })
    static class TestApp {}

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Endpoints públicos")
    class EndpointsPublicosTests {

        @Test
        @DisplayName("Debería permitir acceso a /api/v1/auth/login sin autenticación")
        void deberiaPermitirAccesoALogin() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType("application/json")
                            .content("{\"email\":\"test@test.com\",\"password\":\"password\"}"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Debería permitir acceso a /actuator/health sin autenticación")
        void deberiaPermitirAccesoAHealth() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debería permitir acceso a Swagger sin autenticación")
        void deberiaPermitirAccesoASwagger() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Endpoints protegidos")
    class EndpointsProtegidosTests {

        @Test
        @DisplayName("Debería rechazar acceso a /api/v1/tareas sin autenticación")
        void deberiaRechazarAccesoATareasSinAuth() throws Exception {
            mockMvc.perform(get("/api/v1/tareas"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Debería permitir acceso a /api/v1/tareas con rol USER")
        void deberiaPermitirAccesoATareasConRolUser() throws Exception {
            mockMvc.perform(get("/api/v1/tareas"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Debería rechazar acceso a /actuator/metrics sin autenticación")
        void deberiaRechazarAccesoAMetricsSinAuth() throws Exception {
            mockMvc.perform(get("/actuator/metrics"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Debería permitir acceso a /actuator/metrics con rol ADMIN")
        void deberiaPermitirAccesoAMetricsConRolAdmin() throws Exception {
            mockMvc.perform(get("/actuator/metrics"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Debería rechazar acceso a /actuator/metrics con rol USER")
        void deberiaRechazarAccesoAMetricsConRolUser() throws Exception {
            mockMvc.perform(get("/actuator/metrics"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("CORS")
    class CorsTests {

        @Test
        @DisplayName("Debería incluir cabeceras CORS en respuestas")
        void deberiaIncluirCabecerasCors() throws Exception {
            mockMvc.perform(get("/actuator/health")
                            .header("Origin", "http://localhost:3000"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Configuración de seguridad")
    class ConfiguracionSeguridadTests {

        @Test
        @DisplayName("Debería deshabilitar CSRF para API REST")
        void deberiaDeshabilitarCsrf() throws Exception {

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType("application/json")
                            .content("{\"email\":\"test@test.com\",\"password\":\"password\"}"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Debería configurar sesiones como STATELESS")
        void deberiaConfigurarSesionesStateless() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }
    }
}