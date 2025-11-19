package io.github.jaredmcc4.gtm.util;

import io.github.jaredmcc4.gtm.dto.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Page Util - Unit Tests")
class PageUtilTest {

    @Nested
    @DisplayName("toPageResponse()")
    class ToPageResponseTests {

        @Test
        @DisplayName("Debería convertir Page a PageResponse correctamente")
        void deberiaMapearPageAPageResponse() {

            List<String> content = List.of("Item1", "Item2", "Item3");
            Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 3);
            PageResponse<String> result = PageUtil.toPageResponse(page, item -> item);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent()).containsExactly("Item1", "Item2", "Item3");
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getPageNumber()).isEqualTo(0);
            assertThat(result.getPageSize()).isEqualTo(10);
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("Debería aplicar mapper correctamente")
        void deberiaAplicarMapper() {
            List<Integer> content = List.of(1, 2, 3);
            Page<Integer> page = new PageImpl<>(content, PageRequest.of(0, 10), 3);
            PageResponse<String> result = PageUtil.toPageResponse(page, num -> "Número: " + num);

            assertThat(result.getContent())
                    .containsExactly("Número: 1", "Número: 2", "Número: 3");
        }

        @Test
        @DisplayName("Debería de manejar página vacía")
        void deberiaManejarPaginaVacia() {

            Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            PageResponse<String> result = PageUtil.toPageResponse(page, item -> item);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();
        }

        @Test
        @DisplayName("Debería manejar múltiples páginas correctamente")
        void deberiaManejarMultiplesPaginas() {

            List<String> content = List.of("Item1", "Item2");
            Page<String> page = new PageImpl<>(content, PageRequest.of(1, 2), 5);

            PageResponse<String> result = PageUtil.toPageResponse(page, item -> item);

            assertThat(result.getPageNumber()).isEqualTo(1);
            assertThat(result.getPageSize()).isEqualTo(2);
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.isLast()).isFalse();
        }
    }
}