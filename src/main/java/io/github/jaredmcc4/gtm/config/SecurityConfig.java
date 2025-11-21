package io.github.jaredmcc4.gtm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Secreto HMAC usado para firmar y validar los JWT generados por el backend.
     * Debe tener al menos 256 bits (32 caracteres) para usar HS256 de forma segura.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Lista de origenes permitidos para CORS, separada por comas.
     */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Construye el decodificador JWT con el secreto configurado. Valida que el
     * secreto cumpla el tamano minimo requerido por HS256.
     *
     * @return instancia de {@link JwtDecoder} basada en Nimbus
     * @throws IllegalArgumentException si el secreto tiene menos de 32 bytes
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret debe tener al menos 256 bits (32 caracteres)");
        }

        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    /**
     * Define la cadena principal de filtros de Spring Security:
     * <ul>
     *     <li>Habilita CORS y deshabilita CSRF (API stateless).</li>
     *     <li>Configura sesiones sin estado.</li>
     *     <li>Declara endpoints publicos y restringidos por rol.</li>
     *     <li>Activa el modo Resource Server con JWT.</li>
     * </ul>
     *
     * @param http configuracion HTTP mutable proporcionada por Spring Security
     * @param jwtAuthenticationConverter convertidor de roles desde el claim {@code roles}
     * @return filtro de seguridad completamente configurado
     * @throws Exception si ocurre un error al construir la cadena
     */
    @Bean
    public SecurityFilterChain sfc(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Públicos
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // Restringidos para ADMIN
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Requieren autenticación
                        .requestMatchers("/api/v1/tareas/**").hasRole("USER")
                        .requestMatchers("/api/v1/etiquetas/**").hasRole("USER")
                        .requestMatchers("/api/v1/subtareas/**").hasRole("USER")
                        .requestMatchers("/api/v1/usuarios/**").hasRole("USER")
                        .requestMatchers("/api/v1/adjuntos/**").hasRole("USER")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );
        return http.build();
    }

    /**
     * Configura CORS permitiendo los origenes listados, metodos comunes y cabeceras genericas.
     *
     * @return {@link CorsConfigurationSource} con reglas aplicables a /api/**
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(parseAllowedOrigins());
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", corsConfig);
        return source;
    }

    /**
     * Convierte el claim {@code roles} del JWT en autoridades Spring con prefijo {@code ROLE_}.
     *
     * @return convertidor de autenticacion JWT
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * Proveedor de contrasenas basado en BCrypt con factor de costo 12.
     *
     * @return codificador de contrasenas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Descompone la lista de origenes permitidos en valores individuales sin espacios.
     *
     * @return lista inmutable de origenes permitidos
     */
    private List<String> parseAllowedOrigins() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toList());
    }
}
