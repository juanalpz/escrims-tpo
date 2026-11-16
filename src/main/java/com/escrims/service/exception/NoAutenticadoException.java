package com.escrims.service.exception;

/**
 * Se lanza cuando una operación requiere una sesión válida y la petición
 * no trae credenciales o estas son incorrectas (HTTP 401).
 */
public class NoAutenticadoException extends RuntimeException {

    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
