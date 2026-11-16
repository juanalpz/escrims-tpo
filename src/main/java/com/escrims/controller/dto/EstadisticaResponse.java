package com.escrims.controller.dto;

import com.escrims.domain.entities.Estadistica;

import java.util.UUID;

/**
 * DTO de salida de las estadísticas individuales cargadas para un jugador (RN-13).
 *
 * @param id            Identificador del registro.
 * @param scrimId       Scrim al que pertenece.
 * @param usuarioId     Jugador evaluado.
 * @param kda           KDA en formato {@code kills/deaths/assists}.
 * @param ratioKda      Ratio (kills + assists) / max(1, deaths).
 * @param mvp           Indica si fue el MVP de la partida.
 * @param observaciones Comentarios sobre su desempeño.
 */
public record EstadisticaResponse(
        UUID id,
        UUID scrimId,
        UUID usuarioId,
        String kda,
        double ratioKda,
        boolean mvp,
        String observaciones) {

    public static EstadisticaResponse fromDomain(Estadistica e) {
        return new EstadisticaResponse(e.getId(), e.getScrim().getId(), e.getUsuario().getId(),
                e.getKdaFormateado(), e.calcularRatioKDA(), e.isMvp(), e.getObservaciones());
    }
}
