package com.escrims.service.exception;

/**
 * Se lanza cuando el usuario autenticado no tiene permiso para
 * ejecutar la operación solicitada (HTTP 403).
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
