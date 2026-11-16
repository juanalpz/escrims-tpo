package com.escrims.domain.observer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un evento de dominio publicado cuando todos los jugadores
 * confirman su participación en un scrim, dentro del patrón Observer.
 *
 * @param scrimId identificador del scrim confirmado
 * @param ocurridoEn fecha y hora en que ocurrió la confirmación
 */
public record ConfirmadoEvent(UUID scrimId, LocalDateTime ocurridoEn) implements DomainEvent {

    /**
     * Crea un evento de confirmación con la fecha y hora actuales.
     *
     * @param scrimId identificador del scrim confirmado
     */
    public ConfirmadoEvent(UUID scrimId) {
        this(scrimId, LocalDateTime.now());
    }
}
