package com.escrims.domain.notification;

/**
 * «Abstract Factory» Crea la familia de notifiers (push, email, chat) de un
 * entorno. El dominio depende solo de esta interfaz, nunca de un proveedor
 */
public interface NotifierFactory {

    Notifier createPush();

    Notifier createEmail();

    Notifier createChat();

    /** Devuelve el notifier correspondiente al canal indicado. */
    default Notifier create(Canal canal) {
        if (canal == null) {
            throw new IllegalArgumentException("El canal no puede ser nulo.");
        }
        return switch (canal) {
            case PUSH -> createPush();
            case EMAIL -> createEmail();
            case CHAT -> createChat();
        };
    }
}
