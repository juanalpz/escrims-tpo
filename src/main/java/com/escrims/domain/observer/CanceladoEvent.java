package com.escrims.domain.observer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un evento de dominio publicado al cancelarse un scrim,
 * dentro del patrón Observer.
 *
 * @param scrimId identificador del scrim cancelado
 * @param ocurridoEn fecha y hora en que ocurrió la cancelación
 */
public record CanceladoEvent(UUID scrimId, LocalDateTime ocurridoEn) implements DomainEvent {

    /**
     * Crea un evento de cancelación con la fecha y hora actuales.
     *
     * @param scrimId identificador del scrim cancelado
     */
    public CanceladoEvent(UUID scrimId) {
        this(scrimId, LocalDateTime.now());
    }
}
