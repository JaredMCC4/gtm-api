package io.github.jaredmcc4.gtm.config;

import io.github.jaredmcc4.gtm.dto.auth.OAuthProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades para los proveedores OAuth2/OpenID soportados.
 */
@Configuration
@ConfigurationProperties(prefix = "oauth")
@Getter
@Setter
public class OAuthProviderProperties {

    private Provider google = Provider.googleDefaults();
    private Provider github = Provider.githubDefaults();

    public Provider getProvider(OAuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> google;
            case GITHUB -> github;
        };
    }

    @Getter
    @Setter
    public static class Provider {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userInfoUri;
        private String emailsUri;

        public static Provider googleDefaults() {
            Provider provider = new Provider();
            provider.setTokenUri("https://oauth2.googleapis.com/token");
            provider.setUserInfoUri("https://www.googleapis.com/oauth2/v3/userinfo");
            return provider;
        }

        public static Provider githubDefaults() {
            Provider provider = new Provider();
            provider.setTokenUri("https://github.com/login/oauth/access_token");
            provider.setUserInfoUri("https://api.github.com/user");
            provider.setEmailsUri("https://api.github.com/user/emails");
            return provider;
        }
    }
}
