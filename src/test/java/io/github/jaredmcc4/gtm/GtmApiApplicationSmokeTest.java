package io.github.jaredmcc4.gtm;

import io.github.jaredmcc4.gtm.controller.*;
import io.github.jaredmcc4.gtm.repository.*;
import io.github.jaredmcc4.gtm.services.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@TestPropertySource(locations = "classpath:application.properties")
@DisplayName("GTM API - Smoke Tests")
class GtmApiApplicationSmokeTest {

    @Autowired(required = false)
    private AuthController authController;

    @Autowired(required = false)
    private TareaController tareaController;

    @Autowired(required = false)
    private UsuarioController usuarioController;

    @Autowired(required = false)
    private EtiquetaController etiquetaController;

    @Autowired(required = false)
    private SubtareaController subtareaController;

    @Autowired(required = false)
    private AdjuntoController adjuntoController;

    @Autowired(required = false)
    private AuthService authService;

    @Autowired(required = false)
    private TareaService tareaService;

    @Autowired(required = false)
    private UsuarioService usuarioService;

    @Autowired(required = false)
    private UsuarioRepository usuarioRepository;

    @Autowired(required = false)
    private TareaRepository tareaRepository;

    @Test
    @DisplayName("El contexto de Spring debería cargarse correctamente")
    void contextLoads() {

    }

    @Test
    @DisplayName("Todos los controladores deberían estar cargados")
    void todosLosControladoresDeberianEstarCargados() {
        assertThat(authController).isNotNull();
        assertThat(tareaController).isNotNull();
        assertThat(usuarioController).isNotNull();
        assertThat(etiquetaController).isNotNull();
        assertThat(subtareaController).isNotNull();
        assertThat(adjuntoController).isNotNull();
    }

    @Test
    @DisplayName("Todos los servicios deberían estar cargados")
    void todosLosServiciosDeberianEstarCargados() {
        assertThat(authService).isNotNull();
        assertThat(tareaService).isNotNull();
        assertThat(usuarioService).isNotNull();
    }

    @Test
    @DisplayName("Todos los repositorios deberían estar cargados")
    void todosLosRepositoriosDeberianEstarCargados() {
        assertThat(usuarioRepository).isNotNull();
        assertThat(tareaRepository).isNotNull();
    }
}