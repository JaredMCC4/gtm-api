package io.github.jaredmcc4.gtm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Implementación de {@link TurnstileService} que verifica tokens con la API de Cloudflare.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TurnstileServiceImpl implements TurnstileService {

    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private final RestClient.Builder restClientBuilder;

    @Value("${turnstile.secret-key:}")
    private String secretKey;

    @Override
    public boolean verificarToken(String token, String remoteIp) {
        if (!StringUtils.hasText(secretKey)) {
            log.warn("Turnstile secret key no configurado, omitiendo verificación");
            return true;
        }

        if (!StringUtils.hasText(token)) {
            log.warn("Token de Turnstile vacío");
            throw new IllegalArgumentException("El token de verificación es requerido.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secretKey);
        form.add("response", token);
        if (StringUtils.hasText(remoteIp)) {
            form.add("remoteip", remoteIp);
        }

        try {
            Map<String, Object> response = restClientBuilder.build()
                    .post()
                    .uri(TURNSTILE_VERIFY_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response == null) {
                log.error("Respuesta nula de Turnstile");
                throw new IllegalArgumentException("No se pudo verificar el captcha.");
            }

            Boolean success = (Boolean) response.get("success");
            if (Boolean.TRUE.equals(success)) {
                log.debug("Token de Turnstile verificado correctamente");
                return true;
            }

            log.warn("Verificación de Turnstile fallida: {}", response.get("error-codes"));
            throw new IllegalArgumentException("Verificación de captcha fallida.");

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al verificar token de Turnstile", e);
            throw new IllegalArgumentException("Error al verificar el captcha.");
        }
    }
}
