package io.github.jaredmcc4.gtm.util;

import io.github.jaredmcc4.gtm.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PageUtil {

    /**
     * Convierte una pagina de entidades JPA a una respuesta paginada de DTOs.
     * Mantiene metadatos de paginacion y evita retornar nulidad en colecciones.
     *
     * @param page pagina de entidades origen
     * @param mapper funcion que transforma cada elemento de la pagina
     * @param <T> tipo de entidad origen
     * @param <D> tipo de DTO destino
     * @return {@link PageResponse} con contenido mapeado y datos de paginacion
     */
    public static <T,D>PageResponse<D> toPageResponse(Page<T> page, Function<T,D> mapper) {
        List<D> content = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return PageResponse.<D>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
