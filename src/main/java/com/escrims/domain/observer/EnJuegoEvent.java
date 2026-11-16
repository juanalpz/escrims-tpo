package com.escrims.domain.observer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un evento de dominio publicado cuando un scrim
 * inicia su partida y transiciona al estado EnJuego, dentro del patrón Observer.
 *
 * @param scrimId identificador del scrim iniciado
 * @param ocurridoEn fecha y hora en que ocurrió el inicio de la partida
 */
public record EnJuegoEvent(UUID scrimId, LocalDateTime ocurridoEn) implements DomainEvent {

    /**
     * Crea un evento de inicio de partida con la fecha y hora actuales.
     *
     * @param scrimId identificador del scrim iniciado
     */
    public EnJuegoEvent(UUID scrimId) {
        this(scrimId, LocalDateTime.now());
    }
}
