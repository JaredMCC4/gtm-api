package io.github.jaredmcc4.gtm.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Flyway Migrations - Integration Tests")
class FlywayMigrationTest {

    /**
     * Este test solo se ejecuta cuando hay una base de datos MySQL configurada.
     * Para ejecutarlo, configure la variable de entorno TEST_DB_URL apuntando a MySQL.
     * En el CI/CD o entorno local con H2, este test se salta automáticamente.
     */
    @Test
    @DisplayName("Deberia aplicar las migraciones de Flyway automaticamente")
    @EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".*mysql.*")
    void deberiaAplicarMigracionesFlyway() throws IOException {
        Properties properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
        PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}", ":", true);
        PropertyPlaceholderHelper.PlaceholderResolver resolver = key -> Optional.ofNullable(System.getenv(key))
                .orElse(System.getProperty(key));

        String url = placeholderHelper.replacePlaceholders(
                properties.getProperty("spring.datasource.url"),
                resolver
        );
        String username = placeholderHelper.replacePlaceholders(
                properties.getProperty("spring.datasource.username"),
                resolver
        );
        String password = placeholderHelper.replacePlaceholders(
                properties.getProperty("spring.datasource.password"),
                resolver
        );
        String locations = placeholderHelper.replacePlaceholders(
                properties.getProperty("spring.flyway.locations", "classpath:db/migration"),
                resolver
        );
        boolean baseline = Boolean.parseBoolean(
                properties.getProperty("spring.flyway.baseline-on-migrate", "false")
        );

        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .locations(locations)
                .baselineOnMigrate(baseline)
                .load();

        flyway.migrate();

        assertThat(flyway.info().applied()).isNotEmpty();
    }
}
