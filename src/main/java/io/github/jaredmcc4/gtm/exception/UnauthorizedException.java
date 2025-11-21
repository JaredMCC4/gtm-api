package io.github.jaredmcc4.gtm.exception;

public class UnauthorizedException extends RuntimeException{
    /**
     * Excepcion para indicar que el usuario autenticado no tiene permisos
     * o que no se proporciono un token valido.
     *
     * @param message detalle del motivo
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
