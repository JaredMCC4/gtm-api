package io.github.jaredmcc4.gtm.exception;

public class DuplicateResourceException extends RuntimeException{
    /**
     * Excepcion lanzada cuando se intenta crear un recurso ya existente
     * (por ejemplo, etiqueta o usuario con el mismo identificador unico).
     *
     * @param message detalle del conflicto
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
