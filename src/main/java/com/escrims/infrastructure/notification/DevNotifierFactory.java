package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.Canal;

/**
 * Implementa una ConcreteFactory del patrón Abstract Factory para crear
 * notificadores de desarrollo mediante {@link LogNotifier}.
 */
public class DevNotifierFactory implements NotifierFactory {

    /**
     * Crea un notificador basado en logs, independientemente
     * del canal solicitado.
     *
     * @param canal canal de notificación solicitado
     * @return instancia de LogNotifier para registrar las notificaciones
     */
    @Override
    public Notifier crearNotifier(Canal canal) {
        // En desarrollo, todos los canales utilizan un notificador basado en logs.
        return new LogNotifier();
    }
}
