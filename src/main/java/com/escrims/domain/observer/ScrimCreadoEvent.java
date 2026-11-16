package com.escrims.domain.observer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un evento de dominio publicado cuando se crea un nuevo scrim,
 * dentro del patrón Observer.
 *
 * @param scrimId identificador del scrim creado
 * @param ocurridoEn fecha y hora en que ocurrió la creación
 */
public record ScrimCreadoEvent(UUID scrimId, LocalDateTime ocurridoEn) implements DomainEvent {

    /**
     * Crea un evento de creación de scrim con la fecha y hora actuales.
     *
     * @param scrimId identificador del scrim creado
     */
    public ScrimCreadoEvent(UUID scrimId) {
        this(scrimId, LocalDateTime.now());
    }
}
