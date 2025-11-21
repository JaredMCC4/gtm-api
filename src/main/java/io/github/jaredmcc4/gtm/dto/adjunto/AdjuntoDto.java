package io.github.jaredmcc4.gtm.dto.adjunto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Schema(description = "Archivo adjunto asociado a una tarea.")
public class AdjuntoDto {

    @Schema(description = "Identificador del adjunto", example = "10")
    private Long id;
    @Schema(description = "Nombre original del archivo", example = "captura.png")
    private String nombre;
    @Schema(description = "Mime type reportado", example = "image/png")
    private String mimeType;
    @Schema(description = "Tamaño en bytes", example = "204800")
    private Long sizeBytes;
    @Schema(description = "Ruta o referencia de almacenamiento", example = "uploads/user-1/tarea-5/captura.png")
    private String path;

    @Schema(description = "Fecha de última actualización", example = "2025-11-20T12:34:56")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
