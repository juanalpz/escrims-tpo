package com.escrims.domain.observer;

import java.time.LocalDateTime;

/**
 * Define el contrato base de los eventos de dominio de eScrims,
 * utilizados para comunicar cambios del sistema mediante el patrón Observer.
 */
public interface DomainEvent {

    /**
     * Obtiene la fecha y hora en que ocurrió el evento.
     *
     * @return fecha y hora de ocurrencia del evento
     */
    LocalDateTime ocurridoEn();

    /**
     * Obtiene el tipo de evento a partir del nombre simple
     * de su clase concreta.
     *
     * @return nombre del tipo de evento
     */
    default String obtenerTipo() {
        return getClass().getSimpleName();
    }
}
