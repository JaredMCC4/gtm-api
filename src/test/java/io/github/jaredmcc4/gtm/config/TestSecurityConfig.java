package io.github.jaredmcc4.gtm.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        JwtDecoder decoder = mock(JwtDecoder.class);

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "HS256")
                .claim("sub", "test@example.com")
                .claim("usuarioId", 1L)
                .claim("roles", java.util.List.of("USER"))
                .build();

        when(decoder.decode(anyString())).thenReturn(jwt);
        return decoder;
    }
}
