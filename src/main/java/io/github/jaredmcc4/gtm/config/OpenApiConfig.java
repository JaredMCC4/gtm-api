package io.github.jaredmcc4.gtm.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Gestor de Tareas Moderno - GTM API",
                version = "1.0.0",
                description = "API REST para autenticacion, gestion de tareas, subtareas, etiquetas, adjuntos y perfil de usuario.",
                contact = @Contact(name = "Jared Ch", email = "jaredjosue888@gmail.com", url = "https://github.com/JaredMCC4"),
                license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")
        ),
        servers = {
                @Server(url = "http://localhost:2828", description = "Servidor de desarrollo")
        },
        security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
)
@SecurityScheme(
        name = OpenApiConfig.SECURITY_SCHEME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Usa el token Bearer obtenido en /api/v1/auth/login"
)
public class OpenApiConfig {

    public static final String SECURITY_SCHEME = "bearerAuth";

    /**
     * Ajusta el modelo OpenAPI agregando el esquema de seguridad bearer y dejando
     * consistente el documento generado por springdoc.
     *
     * @return instancia de {@link OpenAPI} con configuracion de seguridad aplicada
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Gestor de Tareas Moderno - GTM API")
                        .version("1.0.0"))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME,
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingrese el token JWT obtenido del endpoint /api/v1/auth/login")));
    }
}
