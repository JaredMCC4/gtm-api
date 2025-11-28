package io.github.jaredmcc4.gtm.services;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.jaredmcc4.gtm.config.OAuthProviderProperties;
import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.auth.JwtResponse;
import io.github.jaredmcc4.gtm.dto.auth.OAuthProvider;
import io.github.jaredmcc4.gtm.dto.auth.OAuthUserInfo;
import io.github.jaredmcc4.gtm.dto.auth.SocialLoginRequest;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.repository.RolRepository;
import io.github.jaredmcc4.gtm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Login social: intercambia el code/access token con el proveedor, obtiene el perfil y emite JWT propios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocialAuthServiceImpl implements SocialAuthService {

    private final OAuthProviderProperties oauthProperties;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final RestClient.Builder restClientBuilder;

    @Override
    @Transactional
    public JwtResponse login(SocialLoginRequest request) {
        OAuthProvider provider = Objects.requireNonNull(request.getProvider(), "El proveedor es obligatorio");
        OAuthProviderProperties.Provider cfg = oauthProperties.getProvider(provider);
        validarConfiguracion(cfg, provider);

        String accessToken = StringUtils.hasText(request.getAccessToken())
                ? request.getAccessToken()
                : intercambiarCodigoPorToken(cfg, provider, request);

        OAuthUserInfo userInfo = obtenerPerfil(provider, cfg, accessToken);
        Usuario usuario = usuarioRepository.findByEmail(userInfo.email())
                .orElseGet(() -> crearUsuarioDesdeOAuth(userInfo, provider));

        if (!usuario.isActivo()) {
            throw new UnauthorizedException("El usuario está desactivado.");
        }

        return authService.emitirTokensParaUsuario(usuario);
    }

    private void validarConfiguracion(OAuthProviderProperties.Provider cfg, OAuthProvider provider) {
        if (!StringUtils.hasText(cfg.getClientId()) || !StringUtils.hasText(cfg.getClientSecret())) {
            throw new IllegalStateException("Faltan credenciales (clientId/clientSecret) para " + provider);
        }
        if (!StringUtils.hasText(cfg.getTokenUri()) || !StringUtils.hasText(cfg.getUserInfoUri())) {
            throw new IllegalStateException("Faltan endpoints token/userinfo para " + provider);
        }
    }

    private String intercambiarCodigoPorToken(OAuthProviderProperties.Provider cfg, OAuthProvider provider, SocialLoginRequest request) {
        if (!StringUtils.hasText(request.getCode())) {
            throw new BadCredentialsException("No se recibió authorization code ni access token.");
        }

        String redirectUri = StringUtils.hasText(request.getRedirectUri()) ? request.getRedirectUri() : cfg.getRedirectUri();
        if (!StringUtils.hasText(redirectUri)) {
            throw new IllegalStateException("Debes configurar un redirectUri para " + provider + " o enviarlo en la solicitud.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", cfg.getClientId());
        form.add("client_secret", cfg.getClientSecret());
        form.add("code", request.getCode());
        form.add("redirect_uri", redirectUri);
        if (provider != OAuthProvider.GITHUB) {
            form.add("grant_type", "authorization_code");
        }

        log.info("Intercambiando authorization code con {}", provider);
        Map<String, Object> tokenResponse = restClientBuilder.build()
                .post()
                .uri(cfg.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(new TypeReference<>() {});

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new IllegalStateException("No se pudo obtener el access token de " + provider);
        }
        return Objects.toString(tokenResponse.get("access_token"));
    }

    private OAuthUserInfo obtenerPerfil(OAuthProvider provider, OAuthProviderProperties.Provider cfg, String accessToken) {
        return switch (provider) {
            case GOOGLE -> parseOpenIdProfile(requestUserInfo(cfg.getUserInfoUri(), accessToken), provider);
            case GITHUB -> parseGithubProfile(cfg, accessToken);
        };
    }

    private Map<String, Object> requestUserInfo(String uri, String accessToken) {
        return restClientBuilder.build()
                .get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new TypeReference<>() {});
    }

    private OAuthUserInfo parseOpenIdProfile(Map<String, Object> userInfo, OAuthProvider provider) {
        if (userInfo == null) {
            throw new IllegalStateException("No se pudo obtener el perfil de " + provider);
        }
        String email = Objects.toString(userInfo.get("email"), null);
        if (!StringUtils.hasText(email)) {
            throw new IllegalStateException("El proveedor " + provider + " no devolvió un email.");
        }
        String name = Objects.toString(userInfo.getOrDefault("name", userInfo.get("given_name")), email);
        String sub = Objects.toString(userInfo.get("sub"), null);
        return new OAuthUserInfo(email, name, sub);
    }

    private OAuthUserInfo parseGithubProfile(OAuthProviderProperties.Provider cfg, String accessToken) {
        Map<String, Object> userProfile = restClientBuilder.build()
                .get()
                .uri(cfg.getUserInfoUri())
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new TypeReference<>() {});

        if (userProfile == null) {
            throw new IllegalStateException("No se pudo obtener el perfil de GitHub.");
        }

        String email = Objects.toString(userProfile.get("email"), null);
        if (!StringUtils.hasText(email) && StringUtils.hasText(cfg.getEmailsUri())) {
            email = obtenerEmailPrimarioGithub(cfg.getEmailsUri(), accessToken);
        }
        if (!StringUtils.hasText(email)) {
            throw new IllegalStateException("GitHub no devolvió un email. Asegúrate de pedir el scope user:email.");
        }

        String name = Objects.toString(userProfile.get("name"), null);
        if (!StringUtils.hasText(name)) {
            name = Objects.toString(userProfile.get("login"), email);
        }
        String id = Objects.toString(userProfile.get("id"), null);

        return new OAuthUserInfo(email, name, id);
    }

    private String obtenerEmailPrimarioGithub(String emailsUri, String accessToken) {
        List<Map<String, Object>> emails = restClientBuilder.build()
                .get()
                .uri(emailsUri)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new TypeReference<>() {});

        if (emails == null || emails.isEmpty()) {
            return null;
        }

        return emails.stream()
                .filter(map -> Boolean.TRUE.equals(map.get("primary")))
                .map(map -> Objects.toString(map.get("email"), null))
                .filter(StringUtils::hasText)
                .findFirst()
                .orElseGet(() -> emails.stream()
                        .map(map -> Objects.toString(map.get("email"), null))
                        .filter(StringUtils::hasText)
                        .findFirst()
                        .orElse(null));
    }

    private Usuario crearUsuarioDesdeOAuth(OAuthUserInfo userInfo, OAuthProvider provider) {
        Rol rolUser = rolRepository.findByNombreRol("USER")
                .orElseThrow(() -> new ResourceNotFoundException("El rol de usuario no existe."));

        String randomPassword = "oauth-" + provider.name().toLowerCase() + "-" + UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .email(userInfo.email())
                .contrasenaHash(passwordEncoder.encode(randomPassword))
                .nombreUsuario(StringUtils.hasText(userInfo.displayName()) ? userInfo.displayName() : userInfo.email())
                .activo(true)
                .roles(new HashSet<>(List.of(rolUser)))
                .build();

        return usuarioRepository.save(usuario);
    }
}
