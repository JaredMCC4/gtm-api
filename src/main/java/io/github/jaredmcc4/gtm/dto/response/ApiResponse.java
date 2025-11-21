package io.github.jaredmcc4.gtm.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta estandar de la API. success=true cuando la operacion fue exitosa.")
public class ApiResponse<T> {

    @Schema(description = "Indica si la operacion fue exitosa.", example = "true")
    private Boolean success;
    @Schema(description = "Payload principal de la respuesta.")
    private T data;
    @Schema(description = "Mensaje informativo o de error.", example = "Operacion realizada con exito")
    private String message;
    @Schema(description = "Detalles adicionales de error cuando success=false.")
    private Object errors;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
