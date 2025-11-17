package io.github.jaredmcc4.gtm.util;

import io.github.jaredmcc4.gtm.dto.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Page Util - Unit Tests")
class PageUtilTest {

    private PageUtil pageUtil;

    @BeforeEach
    void setUp() {
        pageUtil = new PageUtil();
    }

    @Test
    @DisplayName("Debería mapear Page a PageResponse correctamente")
    void deberiaMapearPageAPageResponse() {

        List<String> content = List.of("Item1", "Item2", "Item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 3);
        PageResponse<String> result = pageUtil.toPageResponse(page);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("Debería de manejar página vacía")
    void deberiaManejarPaginaVacia() {

        Page<String> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        PageResponse<String> result = pageUtil.toPageResponse(emptyPage);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("Debería de identificar la primera y última página correctamente")
    void deberiaIdentificarPrimeraYUltimaPagina() {

        List<String> content = List.of("Item1", "Item2");
        Page<String> firstPage = new PageImpl<>(content, PageRequest.of(0, 2), 10);
        Page<String> middlePage = new PageImpl<>(content, PageRequest.of(2, 2), 10);
        Page<String> lastPage = new PageImpl<>(content, PageRequest.of(4, 2), 10);

        PageResponse<String> first = pageUtil.toPageResponse(firstPage);
        PageResponse<String> middle = pageUtil.toPageResponse(middlePage);
        PageResponse<String> last = pageUtil.toPageResponse(lastPage);

        assertThat(first.isFirst()).isTrue();
        assertThat(first.isLast()).isFalse();

        assertThat(middle.isFirst()).isFalse();
        assertThat(middle.isLast()).isFalse();

        assertThat(last.isFirst()).isFalse();
        assertThat(last.isLast()).isTrue();
    }
}