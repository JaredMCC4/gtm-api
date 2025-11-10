package io.github.jaredmcc4.gtm.dto.subtarea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubtareaDto {

    private Long id;

    @NotBlank(message = "El título no puede estar vacío.")
    @Size(max = 120, message = "El título no puede exceder los {max} caracteres.")
    private String titulo;

    @Builder.Default
    private Boolean completada = false;
}
