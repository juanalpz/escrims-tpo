package com.escrims.controller.exception;

import com.escrims.service.exception.AccesoDenegadoException;
import com.escrims.service.exception.NoAutenticadoException;
import com.escrims.service.exception.NoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para estandarizar las respuestas de error HTTP en la API REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura errores de validación de campos en DTOs anotados con {@code @Valid} (HTTP 400).
     *
     * @param ex      excepción con el detalle de los campos inválidos.
     * @param request petición HTTP entrante donde ocurrió el error.
     * @return respuesta HTTP 400 con el mapa de errores de validación por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponse body = ErrorResponse.conValidaciones(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los datos enviados",
                "Uno o más campos no cumplen con las restricciones requeridas.",
                request.getRequestURI(),
                errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Captura errores de deserialización JSON o valores enumerados inválidos en el cuerpo de la petición (HTTP 400).
     *
     * @param ex      excepción producida al leer el cuerpo de la solicitud.
     * @param request petición HTTP entrante donde ocurrió el error.
     * @return respuesta HTTP 400 con el detalle del formato inválido.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Cuerpo de solicitud inválido",
                "El formato JSON o alguno de los valores enumerados (Juego, Formato, Región, Rol, Rango) no es válido.",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Captura argumentos o parámetros que incumplen reglas de validación de dominio (HTTP 400).
     *
     * @param ex      excepción con el mensaje de la regla infringida.
     * @param request petición HTTP entrante donde ocurrió el error.
     * @return respuesta HTTP 400 con el detalle del parámetro inválido.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Parámetro o regla de entrada inválida",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Captura excepciones cuando una entidad o recurso solicitado no existe en el sistema (HTTP 404).
     *
     * @param ex      excepción con la descripción del recurso no encontrado.
     * @param request petición HTTP entrante donde ocurrió el error.
     * @return respuesta HTTP 404 con el detalle del recurso faltante.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            RecursoNoEncontradoException ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Captura búsquedas de entidades inexistentes hechas por los servicios y rutas sin recurso (HTTP 404).
     *
     * @param ex      excepción con la descripción del recurso no encontrado.
     * @param request petición HTTP entrante donde ocurrió el error.
     * @return respuesta HTTP 404 con el detalle del recurso faltante.
     */
    @ExceptionHandler({NoEncontradoException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNoEncontrado(Exception ex, HttpServletRequest request) {
        String mensaje = ex instanceof NoEncontradoException ? ex.getMessage() : "La ruta solicitada no existe.";
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                mensaje,
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Captura peticiones sin credenciales válidas (HTTP 401).
     *
     * @param ex      excepción con el motivo del rechazo.
     * @param request petición HTTP entrante donde ocurrió el error.
     * @return respuesta HTTP 401.
     */
    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<ErrorResponse> handleNoAutenticado(
            NoAutenticadoException ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "No autenticado",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Captura operaciones que el usuario autenticado no tiene permitido realizar (HTTP 403).
     *
     * @param ex      excepción con el motivo del rechazo.
     * @param request petición HTTP entrante donde ocurrió el error.
     * @return respuesta HTTP 403.
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> handleAccesoDenegado(
            AccesoDenegadoException ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Acceso denegado",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Captura parámetros de ruta o de consulta con un tipo inválido, como un UUID mal formado (HTTP 400).
     *
     * @param ex      excepción con el nombre del parámetro inválido.
     * @param request petición HTTP entrante donde ocurrió el error.
     * @return respuesta HTTP 400.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Parámetro o regla de entrada inválida",
                "El valor del parámetro '" + ex.getName() + "' no es válido.",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Captura conflictos de estado del ciclo de vida o restricciones de negocio activas (HTTP 409).
     *
     * @param ex      excepción lanzada por una transición o acción no permitida en el estado actual.
     * @param request petición HTTP entrante donde ocurrió el error.
     * @return respuesta HTTP 409 con el motivo del conflicto.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflicto de estado o regla de negocio",
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Captura cualquier excepción no controlada para evitar exponer trazas internas al cliente (HTTP 500).
     *
     * @param ex      excepción inesperada ocurrida durante el procesamiento.
     * @param request petición HTTP entrante donde ocurrió el error.
     * @return respuesta HTTP 500 con un mensaje genérico de error interno.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "Ocurrió un error inesperado al procesar la solicitud.",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
