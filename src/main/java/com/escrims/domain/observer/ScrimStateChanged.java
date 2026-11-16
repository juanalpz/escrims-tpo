package com.escrims.domain.observer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un evento de dominio publicado cuando un scrim cambia
 * de estado en su ciclo de vida, dentro de los patrones State y Observer.
 *
 * @param scrimId identificador del scrim cuyo estado cambió
 * @param estadoAnterior estado del scrim antes de la transición
 * @param nuevoEstado estado del scrim después de la transición
 * @param ocurridoEn fecha y hora en que ocurrió el cambio de estado
 */
public record ScrimStateChanged(
        UUID scrimId,
        String estadoAnterior,
        String nuevoEstado,
        LocalDateTime ocurridoEn) implements DomainEvent {

    /**
     * Crea un evento de cambio de estado con la fecha y hora actuales.
     *
     * @param scrimId identificador del scrim cuyo estado cambió
     * @param estadoAnterior estado previo a la transición
     * @param nuevoEstado estado resultante de la transición
     */
    public ScrimStateChanged(UUID scrimId, String estadoAnterior, String nuevoEstado) {
        this(scrimId, estadoAnterior, nuevoEstado, LocalDateTime.now());
    }
}
