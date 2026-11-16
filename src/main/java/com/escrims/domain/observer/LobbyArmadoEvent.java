package com.escrims.domain.observer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un evento de dominio publicado cuando se completan los cupos
 * de un scrim y se conforma el lobby, dentro del patrón Observer.
 *
 * @param scrimId identificador del scrim cuyo lobby fue conformado
 * @param ocurridoEn fecha y hora en que se completó el lobby
 */
public record LobbyArmadoEvent(UUID scrimId, LocalDateTime ocurridoEn) implements DomainEvent {

    /**
     * Crea un evento de conformación del lobby con la fecha y hora actuales.
     *
     * @param scrimId identificador del scrim cuyo lobby fue conformado
     */
    public LobbyArmadoEvent(UUID scrimId) {
        this(scrimId, LocalDateTime.now());
    }
}
