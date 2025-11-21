package io.github.jaredmcc4.gtm.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
     * Excepcion para indicar que un recurso de dominio (tarea, subtarea, etiqueta, etc.) no existe.
     *
     * @param message mensaje descriptivo
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
