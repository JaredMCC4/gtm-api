package io.github.jaredmcc4.gtm.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Payload para iniciar sesión usando proveedores externos (Google, Microsoft, GitHub).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para login con OAuth (Google, Microsoft, GitHub).")
public class SocialLoginRequest {

    @Schema(description = "Proveedor de identidad soportado", example = "GOOGLE")
    @NotNull(message = "El proveedor es obligatorio.")
    private OAuthProvider provider;

    @Schema(description = "Código de autorización devuelto por el proveedor (grant authorization_code).", example = "4/0AfJohXk...")
    private String code;

    @Schema(description = "Access token directo si el cliente ya lo obtuvo (opcional).", example = "ya29.a0AfH6SMB...")
    private String accessToken;

    @Schema(description = "redirect_uri registrado en el proveedor. Si no se envía se usa el configurado en el backend.", example = "http://localhost:3000/auth/callback/google")
    private String redirectUri;

    /**
     * Validación simple: se debe recibir el authorization code o un access token directo.
     */
    @AssertTrue(message = "Debe incluir el authorization code o un access token.")
    public boolean isCodeOrTokenPresent() {
        return StringUtils.hasText(code) || StringUtils.hasText(accessToken);
    }
}
