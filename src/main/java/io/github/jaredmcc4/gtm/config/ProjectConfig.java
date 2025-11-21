package io.github.jaredmcc4.gtm.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class ProjectConfig {

    /**
     * Fuente de mensajes para i18n basada en los bundles {@code messages*}.
     *
     * @return {@link MessageSource} con codificacion UTF-8 y sin fallback al locale del sistema
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * Fija el locale por defecto a {@code es-CR} para toda la aplicacion web.
     *
     * @return {@link LocaleResolver} con localidad fija
     */
    @Bean
    public LocaleResolver localeResolver() {
        FixedLocaleResolver resolver = new FixedLocaleResolver();
        resolver.setDefaultLocale(Locale.of("es", "CR"));
        return resolver;
    }
}
