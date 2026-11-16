package com.escrims.controller.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        Map<String, String> erroresValidacion) {
    public static ErrorResponse of(int status, String error, String mensaje, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, path, Map.of());
    }

    public static ErrorResponse conValidaciones(int status, String error, String mensaje, String path,
            Map<String, String> campos) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, path, campos);
    }
}
