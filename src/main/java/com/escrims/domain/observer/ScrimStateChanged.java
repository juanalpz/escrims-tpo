package com.escrims.domain.observer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento publicado cada vez que un Scrim transiciona de estado en su ciclo de
 * vida.
 */
public record ScrimStateChanged(
        UUID scrimId,
        String estadoAnterior,
        String nuevoEstado,
        LocalDateTime ocurridoEn) implements DomainEvent {

    public ScrimStateChanged(UUID scrimId, String estadoAnterior, String nuevoEstado) {
        this(scrimId, estadoAnterior, nuevoEstado, LocalDateTime.now());
    }
}
