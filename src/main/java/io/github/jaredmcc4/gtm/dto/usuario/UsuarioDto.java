package io.github.jaredmcc4.gtm.dto.usuario;

import io.github.jaredmcc4.gtm.dto.rol.RolDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO de salida para exponer datos visibles del usuario.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDto {
    @Schema(example = "1")
    private Long id;
    @Schema(description = "Correo electrónico", example = "user@example.com")
    private String email;
    @Schema(description = "Nombre de usuario para mostrar", example = "Juan Perez")
    private String nombreUsuario;
    @Schema(description = "Zona horaria IANA", example = "America/Costa_Rica")
    private String zonaHoraria;
    @Schema(description = "Indica si la cuenta está activa", example = "true")
    private boolean activo;
    @Schema(description = "Roles asociados al usuario")
    private Set<RolDto> roles;
}

