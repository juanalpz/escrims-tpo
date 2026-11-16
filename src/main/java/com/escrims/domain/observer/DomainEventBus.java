package com.escrims.domain.observer;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementa el Subject del patrón Observer, gestionando la suscripción
 * de observadores y la publicación de eventos de dominio de eScrims.
 */
public class DomainEventBus {

    private final Set<Subscriber> subscribers = new LinkedHashSet<>();

    /**
     * Registra un suscriptor para recibir eventos de dominio.
     *
     * @param subscriber observador que se desea registrar
     * @throws IllegalArgumentException si el suscriptor es nulo
     */
    public void subscribe(Subscriber subscriber) {
        if (subscriber == null) {
            throw new IllegalArgumentException("El suscriptor no puede ser nulo.");
        }
        subscribers.add(subscriber);
    }

    /**
     * Elimina un suscriptor del bus de eventos.
     *
     * @param subscriber observador que se desea desuscribir
     */
    public void unsubscribe(Subscriber subscriber) {
        if (subscriber != null) {
            subscribers.remove(subscriber);
        }
    }

    /**
     * Publica un evento de dominio y notifica a todos los suscriptores
     * registrados mediante {@link Subscriber#onEvent(DomainEvent)}.
     *
     * @param event evento de dominio que se desea publicar
     * @throws IllegalArgumentException si el evento es nulo
     */
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("El evento de dominio no puede ser nulo.");
        }
        // Se utiliza una copia para permitir desuscripciones durante la notificación.
        for (Subscriber subscriber : Set.copyOf(subscribers)) {
            subscriber.onEvent(event);
        }
    }

    /**
     * Obtiene los suscriptores registrados en el bus de eventos.
     *
     * @return conjunto no modificable de suscriptores
     */
    public Set<Subscriber> getSubscribers() {
        return Collections.unmodifiableSet(subscribers);
    }
}
