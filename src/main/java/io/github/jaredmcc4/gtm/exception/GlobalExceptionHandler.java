package io.github.jaredmcc4.gtm.exception;

import io.github.jaredmcc4.gtm.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

/**
 * Maneja de forma centralizada las excepciones conocidas y las traduce
 * a respuestas homogeneas para los controladores REST.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestiona recursos no encontrados y devuelve 404.
     *
     * @param ex detalle de la excepcion de negocio
     * @return respuesta HTTP 404 con el mensaje para el cliente
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    /**
     * Gestiona argumentos invalidos enviados por el cliente.
     *
     * @param ex excepcion generada durante validaciones o reglas de negocio
     * @return respuesta HTTP 400 con el detalle
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Argumento invA�lido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    /**
     * Construye un mapa de errores de bean validation.
     *
     * @param ex excepcion generada por @Valid
     * @return respuesta HTTP 400 con los campos y mensajes invalidos
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Errores de validaciA3n: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Errores de validaciA3n", errors));
    }

    /**
     * Maneja errores de formato numerico en parametros de entrada.
     *
     * @param ex excepcion de parseo numerico
     * @return respuesta HTTP 400
     */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ApiResponse<Void>> handleNumberFormatException(NumberFormatException ex) {
        log.error("Error de formato de nA�mero: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Formato de nA�mero invA�lido en la peticiA3n", null));
    }

    /**
     * Maneja null pointers para devolver un error controlado al cliente.
     *
     * @param ex excepcion disparada por falta de datos requeridos
     * @return respuesta HTTP 400
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointerException(NullPointerException ex) {
        log.error("Error de null pointer: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Datos requeridos faltantes en la peticiA3n", null));
    }

    /**
     * Fallback generico para excepciones no contempladas.
     *
     * @param ex excepcion inesperada
     * @return respuesta HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error interno del servidor", null));
    }

    /**
     * Maneja accesos no autorizados.
     *
     * @param ex excepcion de autorizacion
     * @return respuesta HTTP 401
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        log.error("Acceso no autorizado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    /**
     * Maneja casos de duplicidad de recursos (ej. email ya existente).
     *
     * @param ex excepcion de duplicado
     * @return respuesta HTTP 409
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(DuplicateResourceException ex) {
        log.error("Recurso duplicado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    /**
     * Maneja intentos de subir archivos demasiado grandes.
     *
     * @param ex excepcion propia del multipart resolver
     * @return respuesta HTTP 413
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxSizeException(MaxUploadSizeExceededException ex){
        log.error("Archivo excede el tamaA�o mA�ximo permitido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("El archivo excede el tamaA�o mA�ximo permitido (10MB)", null));
    }

    /**
     * Gestiona errores provenientes del proveedor de autenticacion.
     *
     * @param ex excepcion de autenticacion
     * @return respuesta HTTP 401
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Error de autenticaciA3n: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Error de autenticaciA3n: " + ex.getMessage(), null));
    }

    /**
     * Maneja credenciales invalidas durante el login.
     *
     * @param ex excepcion disparada por Spring Security
     * @return respuesta HTTP 401
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials (BadCredentialsException ex) {
        log.error("Credenciales invA�lidas.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Datos incorrectos.", null));
    }

    /**
     * Maneja accesos denegados cuando el usuario no tiene el rol requerido.
     *
     * @param ex excepcion de seguridad
     * @return respuesta HTTP 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied (AccessDeniedException ex) {
        log.error("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("No cuenta con el permiso para acceder al recurso.", null));
    }
}
