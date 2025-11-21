package io.github.jaredmcc4.gtm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion Spring Boot del gestor de tareas.
 */
@SpringBootApplication
public class GtmApiApplication {

    /**
     * Arranca el contexto de Spring y expone la API.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(GtmApiApplication.class, args);
    }

}
