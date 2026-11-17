package com.escrims.service.exception;

/**
 * Se lanza cuando un servicio de aplicación no encuentra la entidad
 * solicitada (HTTP 404).
 */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
