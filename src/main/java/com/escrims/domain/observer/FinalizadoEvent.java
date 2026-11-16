package com.escrims.domain.observer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un evento de dominio publicado cuando finaliza un scrim,
 * dentro del patrón Observer.
 *
 * @param scrimId identificador del scrim finalizado
 * @param ocurridoEn fecha y hora en que ocurrió la finalización
 */
public record FinalizadoEvent(UUID scrimId, LocalDateTime ocurridoEn) implements DomainEvent {

    /**
     * Crea un evento de finalización con la fecha y hora actuales.
     *
     * @param scrimId identificador del scrim finalizado
     */
    public FinalizadoEvent(UUID scrimId) {
        this(scrimId, LocalDateTime.now());
    }
}
