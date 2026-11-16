package com.escrims.controller.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de respuesta estándar para errores HTTP capturados por el {@link GlobalExceptionHandler}.
 *
 * @param timestamp         Fecha y hora exacta en que ocurrió el error.
 * @param status            Código de estado HTTP de la respuesta (ej. 400, 404, 409).
 * @param error             Descripción breve del tipo de error HTTP.
 * @param mensaje           Detalle legible del error ocurrido.
 * @param path              Ruta del endpoint donde se produjo la excepción.
 * @param erroresValidacion Mapa con los campos inválidos y sus mensajes de error (si aplica).
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        Map<String, String> erroresValidacion) {

    /**
     * Crea una respuesta de error estándar sin errores de validación por campo.
     *
     * @param status  código de estado HTTP.
     * @param error   nombre o razón del estado HTTP.
     * @param mensaje mensaje descriptivo del error.
     * @param path    URI de la petición que originó el error.
     * @return nueva instancia de {@link ErrorResponse} con el mapa de validaciones vacío.
     */
    public static ErrorResponse of(int status, String error, String mensaje, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, path, Map.of());
    }

    /**
     * Crea una respuesta de error incluyendo el detalle de los campos que fallaron la validación.
     *
     * @param status  código de estado HTTP.
     * @param error   nombre o razón del estado HTTP.
     * @param mensaje mensaje general de validación.
     * @param path    URI de la petición que originó el error.
     * @param campos  mapa con el nombre de cada campo inválido y su descripción.
     * @return nueva instancia de {@link ErrorResponse} con los errores por campo cargados.
     */
    public static ErrorResponse conValidaciones(int status, String error, String mensaje, String path,
            Map<String, String> campos) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, path, campos);
    }
}
