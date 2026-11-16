package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.Canal;

/**
 * Define el contrato AbstractFactory del patrón Abstract Factory
 * para crear notificadores según el canal y el entorno de ejecución.
 */
public interface NotifierFactory {

    /**
     * Crea un notificador para el canal solicitado según
     * la implementación concreta de la fábrica.
     *
     * @param canal canal de notificación para el que se solicita un notificador
     * @return instancia de Notifier correspondiente al canal y entorno configurados
     */
    Notifier crearNotifier(Canal canal);
}
