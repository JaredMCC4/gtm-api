package io.github.jaredmcc4.gtm.i18n;

import io.github.jaredmcc4.gtm.GtmApiApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.TestPropertySource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = GtmApiApplication.class, properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@TestPropertySource(locations = "classpath:application.properties")
@DisplayName("Message Source - Integration Tests")
class MessageSourceTest {

    @Autowired
    private MessageSource messageSource;

    @Test
    @DisplayName("Contexto carga sin DB")
    void contextUp(){
    }

    @Test
    @DisplayName("Debería de cargar mensajes en español")
    void deberiaCargarMensajesEspanol() {

        String mensaje = messageSource.getMessage(
                "validation.email.invalid",
                null,
                Locale.forLanguageTag("es")
        );

        assertThat(mensaje).isNotNull();
        assertThat(mensaje).isNotEmpty();
        assertThat(mensaje).doesNotContain("???");
    }

    @Test
    @DisplayName("Debería de tener todas las claves de validación obligatorias")
    void deberiaTenerClavesValidacion() {
        String[] clavesObligatorias = {
                "validation.email.invalid",
                "validation.email.required",
                "validation.password.required",
                "validation.password.size",
                "validation.titulo.required",
                "validation.titulo.size"
        };

        for (String clave : clavesObligatorias) {
            String mensaje = messageSource.getMessage(
                    clave,
                    null,
                    Locale.forLanguageTag("es")
            );

            assertThat(mensaje)
                    .as("Mensaje para clave: " + clave)
                    .isNotNull()
                    .doesNotContain("???");
        }
    }

    @Test
    @DisplayName("Debería  de soportar parámetros en mensajes")
    void deberiaSoportarParametros() {

        String mensaje = messageSource.getMessage(
                "error.resource.notfound",
                new Object[]{"Tarea", "123"},
                Locale.forLanguageTag("es")
        );

        assertThat(mensaje).contains("Tarea").contains("123");
    }
}