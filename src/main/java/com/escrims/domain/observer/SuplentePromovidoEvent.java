package com.escrims.domain.observer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento publicado cuando un titular se baja y un suplente de la lista de
 * espera
 * toma su lugar (RN-17).
 */
public record SuplentePromovidoEvent(
        UUID scrimId,
        UUID usuarioSalienteId,
        UUID suplentePromovidoId,
        LocalDateTime ocurridoEn) implements DomainEvent {

    public SuplentePromovidoEvent(UUID scrimId, UUID usuarioSalienteId, UUID suplentePromovidoId) {
        this(scrimId, usuarioSalienteId, suplentePromovidoId, LocalDateTime.now());
    }
}
