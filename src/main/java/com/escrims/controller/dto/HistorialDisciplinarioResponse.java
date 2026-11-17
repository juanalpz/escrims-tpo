package com.escrims.controller.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de salida con la situación disciplinaria completa de un usuario.
 *
 * @param usuarioId      Identificador del usuario.
 * @param strikes        Strikes acumulados.
 * @param cooldownActivo Indica si hoy no puede postularse ni organizar (RN-06).
 * @param cooldownHasta  Fin del cooldown vigente, o null si no tiene.
 * @param scrimsJugados  Scrims disputados.
 * @param abandonos      Abandonos registrados.
 * @param noShows        Ausencias registradas.
 * @param sanciones      Sanciones recibidas.
 * @param incidencias    Abandonos y no-shows registrados.
 */
public record HistorialDisciplinarioResponse(
        UUID usuarioId,
        int strikes,
        boolean cooldownActivo,
        LocalDateTime cooldownHasta,
        int scrimsJugados,
        int abandonos,
        int noShows,
        List<SancionResponse> sanciones,
        List<IncidenciaResponse> incidencias) {
}
