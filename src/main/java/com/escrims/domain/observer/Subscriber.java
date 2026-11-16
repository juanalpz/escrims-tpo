package com.escrims.domain.observer;

/**
 * Define el contrato Observer del patrón Observer para los suscriptores
 * que reaccionan a eventos de dominio publicados por el sistema.
 */
public interface Subscriber {

    /**
     * Procesa un evento de dominio recibido del bus de eventos.
     *
     * @param event evento de dominio que debe procesar el suscriptor
     */
    void onEvent(DomainEvent event);
}
