package com.escrims.controller.dto;

import com.escrims.domain.entities.Sancion;
import com.escrims.domain.entities.TipoInfraccion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida de una sanción del sistema de strikes.
 *
 * @param id              Identificador de la sanción.
 * @param tipo            Infracción que la originó.
 * @param strikes         Strikes que sumó.
 * @param cooldownHasta   Fin del cooldown, o null si fue solo una advertencia.
 * @param activa          Indica si el cooldown sigue vigente.
 * @param motivo          Causa informada.
 * @param fechaAplicacion Momento en que se aplicó.
 */
public record SancionResponse(
        UUID id,
        TipoInfraccion tipo,
        int strikes,
        LocalDateTime cooldownHasta,
        boolean activa,
        String motivo,
        LocalDateTime fechaAplicacion) {

    public static SancionResponse fromDomain(Sancion s) {
        return s == null ? null : new SancionResponse(s.getId(), s.getTipo(), s.getStrikes(), s.getCooldownHasta(),
                s.estaActiva(), s.getMotivo(), s.getFechaAplicacion());
    }
}
