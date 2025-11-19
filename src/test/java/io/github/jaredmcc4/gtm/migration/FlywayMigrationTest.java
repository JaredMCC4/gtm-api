package io.github.jaredmcc4.gtm.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Flyway Migrations - Integration Tests")
class FlywayMigrationTest {

    @Test
    @DisplayName("Debería aplicar las migraciones de Flyway automáticamente")
    void deberiaAplicarMigracionesFlyway() throws IOException {
        Properties properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));

        String url = properties.getProperty("spring.datasource.url");
        String username = properties.getProperty("spring.datasource.username");
        String password = properties.getProperty("spring.datasource.password");
        String locations = properties.getProperty("spring.flyway.locations", "classpath:db/migration");
        boolean baseline = Boolean.parseBoolean(properties.getProperty("spring.flyway.baseline-on-migrate", "false"));

        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .locations(locations)
                .baselineOnMigrate(baseline)
                .load();

        assertThat(flyway.info().applied()).isNotEmpty();
    }
}
