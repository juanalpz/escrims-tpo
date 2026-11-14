package com.escrims.domain.observer;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * «Subject» Bus central de eventos de dominio del patrón Observer.
 * Desacopla la lógica del dominio de los canales de notificación (Push, Email,
 * Discord, iCal).
 */
public class DomainEventBus {

    private final Set<Subscriber> subscribers = new LinkedHashSet<>();

    public void subscribe(Subscriber subscriber) {
        if (subscriber == null) {
            throw new IllegalArgumentException("El suscriptor no puede ser nulo.");
        }
        subscribers.add(subscriber);
    }

    public void unsubscribe(Subscriber subscriber) {
        if (subscriber != null) {
            subscribers.remove(subscriber);
        }
    }

    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("El evento de dominio no puede ser nulo.");
        }
        // Iteramos sobre una copia para evitar ConcurrentModificationException si un
        // observer se desuscribe
        for (Subscriber subscriber : Set.copyOf(subscribers)) {
            subscriber.onEvent(event);
        }
    }

    public Set<Subscriber> getSubscribers() {
        return Collections.unmodifiableSet(subscribers);
    }
}
