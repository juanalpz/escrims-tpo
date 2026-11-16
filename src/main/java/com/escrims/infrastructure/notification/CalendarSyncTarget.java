package com.escrims.infrastructure.notification;

import com.escrims.domain.observer.DomainEvent;

/**
 * Define el contrato Target del patrón Adapter para sincronizar
 * eventos de dominio con servicios de calendario externos.
 */
public interface CalendarSyncTarget {

    /**
     * Sincroniza un evento de dominio con un calendario externo.
     *
     * @param evento evento de dominio que se desea sincronizar
     */
    void sincronizar(DomainEvent evento);
}
