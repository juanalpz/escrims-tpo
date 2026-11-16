package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.Canal;

/**
 * Implementa una ConcreteFactory del patrón Abstract Factory para crear
 * notificadores de producción mediante adaptadores de servicios externos.
 */
public class ProdNotifierFactory implements NotifierFactory {

    /**
     * Crea un notificador según el canal solicitado, utilizando
     * los adaptadores de Firebase, SendGrid, Discord o iCalendar.
     *
     * @param canal canal de notificación para el que se solicita un notificador
     * @return instancia de Notifier correspondiente al canal seleccionado
     * @throws NullPointerException si el canal es nulo
     */
    @Override
    public Notifier crearNotifier(Canal canal) {
        return switch (canal) {
            case PUSH    -> new FirebaseAdapter(new FirebaseClient());
            case EMAIL   -> new SendGridAdapter(new SendGridClient());
            case DISCORD -> new DiscordAdapter(new DiscordClient());
            case ICAL    -> new ICalAdapter(new ICalClient());
        };
    }
}
