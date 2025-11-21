package io.github.jaredmcc4.gtm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Representa una pagina de resultados de la API, incluyendo metadatos de paginacion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta paginada estándar.")
public class PageResponse<T> {
    @Schema(description = "Elementos de la página solicitada.")
    private List<T> content;
    @Schema(description = "Número de página (0-based).", example = "0")
    private Integer pageNumber;
    @Schema(description = "Tamaño de página solicitado.", example = "20")
    private Integer pageSize;
    @Schema(description = "Total de elementos disponibles.", example = "125")
    private Long totalElements;
    @Schema(description = "Total de páginas disponibles.", example = "7")
    private Integer totalPages;
    @Schema(description = "Indica si es la última página.", example = "false")
    private Boolean last;

    /**
     * Calcula si la página actual es la última de acuerdo al total de páginas.
     *
     * @return true si es la última página
     */
    public Boolean isLast() {
        return pageNumber >= totalPages - 1;
    }
}

