package com.escrims.domain.observer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un evento de dominio publicado cuando un suplente reemplaza
 * a un jugador titular, dentro del patrón Observer (RN-17).
 *
 * @param scrimId identificador del scrim donde ocurre la promoción
 * @param usuarioSalienteId identificador del jugador titular que abandona el scrim
 * @param suplentePromovidoId identificador del suplente que ocupa su lugar
 * @param ocurridoEn fecha y hora en que ocurrió la promoción
 */
public record SuplentePromovidoEvent(
        UUID scrimId,
        UUID usuarioSalienteId,
        UUID suplentePromovidoId,
        LocalDateTime ocurridoEn) implements DomainEvent {

    /**
     * Crea un evento de promoción de suplente con la fecha y hora actuales.
     *
     * @param scrimId identificador del scrim donde ocurre la promoción
     * @param usuarioSalienteId identificador del jugador titular saliente
     * @param suplentePromovidoId identificador del suplente promovido
     */
    public SuplentePromovidoEvent(UUID scrimId, UUID usuarioSalienteId, UUID suplentePromovidoId) {
        this(scrimId, usuarioSalienteId, suplentePromovidoId, LocalDateTime.now());
    }
}
