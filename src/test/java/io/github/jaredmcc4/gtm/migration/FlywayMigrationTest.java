package io.github.jaredmcc4.gtm.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@DisplayName("Flyway Migrations - Integration Tests")
public class FlywayMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Debería aplicar todas las migraciones de Flyway correctamente")
    void deberiaAplicarMigracionesFlyway() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        int migrationsExecuted = flyway.migrate().migrationsExecuted;
        assertThat(migrationsExecuted).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Debería validar los checksum de migraciones")
    void deberiaValidarChecksum() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        assertThatCode(() -> flyway.validate())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Debería obtener la versión actual del schema")
    void deberiaObtenerVersionActual() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        var info = flyway.info();
        var current = info.current();

        assertThat(current).isNotNull();
        assertThat(current.getVersion()).isNotNull();
    }
}
