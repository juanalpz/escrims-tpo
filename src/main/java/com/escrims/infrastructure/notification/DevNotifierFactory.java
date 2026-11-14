package com.escrims.infrastructure.notification;

import com.escrims.domain.notification.Canal;
import com.escrims.domain.notification.NotifierFactory;

/**
 * «Concrete Factory» Entorno de desarrollo: todos los canales son
 * {@link LogNotifier}, así que nunca se contacta a un proveedor real.
 * Devuelve siempre la misma instancia por canal para poder inspeccionar lo
 * enviado
 */
public class DevNotifierFactory implements NotifierFactory {

    private final LogNotifier push = new LogNotifier(Canal.PUSH);
    private final LogNotifier email = new LogNotifier(Canal.EMAIL);
    private final LogNotifier chat = new LogNotifier(Canal.CHAT);

    @Override
    public LogNotifier createPush() {
        return push;
    }

    @Override
    public LogNotifier createEmail() {
        return email;
    }

    @Override
    public LogNotifier createChat() {
        return chat;
    }
}
