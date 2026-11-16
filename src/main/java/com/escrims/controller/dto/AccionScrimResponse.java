package com.escrims.controller.dto;

/**
 * DTO de salida de una acción administrativa ejecutada sobre el lobby (Patrón Command).
 *
 * @param comando   Comando ejecutado.
 * @param resultado Descripción legible de lo realizado.
 * @param scrim     Resumen actualizado del scrim.
 */
public record AccionScrimResponse(String comando, String resultado, ScrimResponse scrim) {
}
