package io.github.jaredmcc4.gtm.dto.adjunto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que expone metadatos de un archivo adjunto.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdjuntoDto {
    @Schema(example = "1")
    private Long id;
    @Schema(description = "Nombre original del archivo", example = "reporte.pdf")
    private String nombre;
    @Schema(description = "Tipo MIME del archivo", example = "application/pdf")
    private String mimeType;
    @Schema(description = "Tamaño del archivo en bytes", example = "2048")
    private Long sizeBytes;
    @Schema(description = "Ruta de almacenamiento (solo visible internamente en dev)", example = "/uploads/1/reporte.pdf")
    private String path;
    @Schema(description = "Fecha de carga")
    private LocalDateTime updatedAt;
}

