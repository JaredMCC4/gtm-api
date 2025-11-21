package io.github.jaredmcc4.gtm.dto.usuario;

import io.github.jaredmcc4.gtm.dto.rol.RolDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Informacion publica del usuario.")
public class UsuarioDto {

    @Schema(description = "Identificador del usuario", example = "1")
    private Long id;

    @Schema(description = "Email del usuario", example = "admin@example.com")
    private String email;

    @Schema(description = "Nombre visible", example = "GTM_ADMIN")
    private String nombreUsuario;

    @Schema(description = "Zona horaria configurada", example = "America/Costa_Rica")
    private String zonaHoraria;

    @Schema(description = "Indicador de si la cuenta esta activa", example = "true")
    private boolean activo;

    @Schema(description = "Roles asignados")
    private Set<RolDto> roles;
}
