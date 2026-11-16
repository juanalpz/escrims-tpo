package com.escrims.controller.dto;

import com.escrims.domain.entities.RegistroIncidencia;
import com.escrims.domain.entities.TipoInfraccion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida de un abandono o no-show registrado.
 *
 * @param id             Identificador de la incidencia.
 * @param scrimId        Scrim en el que ocurrió.
 * @param usuarioId      Jugador involucrado.
 * @param tipo           ABANDONO o NO_SHOW.
 * @param detalle        Observación de quien la registró.
 * @param fecha          Momento del registro.
 * @param sancion        Sanción aplicada por la incidencia.
 * @param strikesTotales Strikes acumulados del jugador.
 */
public record IncidenciaResponse(
        UUID id,
        UUID scrimId,
        UUID usuarioId,
        TipoInfraccion tipo,
        String detalle,
        LocalDateTime fecha,
        SancionResponse sancion,
        int strikesTotales) {

    public static IncidenciaResponse fromDomain(RegistroIncidencia i) {
        return new IncidenciaResponse(i.getId(), i.getScrim().getId(), i.getUsuario().getId(), i.getTipo(),
                i.getDetalle(), i.getFecha(), SancionResponse.fromDomain(i.getSancion()),
                i.getUsuario().getStrikes());
    }
}
