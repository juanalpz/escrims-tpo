package com.escrims.domain.observer;

import com.escrims.domain.entities.TipoInfraccion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de dominio publicado cuando el sistema de penalidades aplica
 * un strike a un usuario (abandono, no-show o reporte de conducta).
 *
 * @param usuarioId      usuario sancionado
 * @param scrimId        scrim en el que ocurrió la infracción
 * @param tipo           infracción que originó la sanción
 * @param strikesTotales strikes acumulados por el usuario tras la sanción
 * @param cooldownHasta  fin del cooldown aplicado, o null si fue solo una advertencia
 * @param ocurridoEn     momento en que se aplicó la sanción
 */
public record SancionAplicadaEvent(
        UUID usuarioId,
        UUID scrimId,
        TipoInfraccion tipo,
        int strikesTotales,
        LocalDateTime cooldownHasta,
        LocalDateTime ocurridoEn) implements DomainEvent {

    public SancionAplicadaEvent(UUID usuarioId, UUID scrimId, TipoInfraccion tipo,
                                int strikesTotales, LocalDateTime cooldownHasta) {
        this(usuarioId, scrimId, tipo, strikesTotales, cooldownHasta, LocalDateTime.now());
    }
}
