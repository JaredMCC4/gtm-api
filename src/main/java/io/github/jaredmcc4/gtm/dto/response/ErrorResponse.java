package io.github.jaredmcc4.gtm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Esquema estandar de error para las respuestas no exitosas.")
public class ErrorResponse {

    @Schema(description = "Marca de exito, siempre false en errores", example = "false")
    @Builder.Default
    private Boolean success = false;

    @Schema(description = "Mensaje legible del error", example = "Recurso no encontrado")
    private String message;

    @Schema(description = "Detalle del error (lista/mapa de validaciones u otras causas)")
    private Object errors;

    @Schema(description = "Ruta del request que genero el error", example = "/api/v1/tareas/99")
    private String path;

    @Schema(description = "Fecha y hora del error en ISO-8601", example = "2025-11-20T12:34:56")
    private LocalDateTime timestamp;
}
