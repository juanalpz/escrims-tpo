package com.escrims.controller.exception;

/**
 * Excepción lanzada cuando una entidad o recurso solicitado no existe en el sistema.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Construye la excepción con el detalle del recurso no encontrado.
     *
     * @param mensaje descripción del error o identificador faltante.
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
