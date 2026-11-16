package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.Canal;
import com.escrims.domain.observer.DomainEvent;
import com.escrims.domain.observer.LobbyArmadoEvent;
import com.escrims.domain.observer.ScrimCreadoEvent;
import com.escrims.domain.observer.ScrimStateChanged;
import com.escrims.domain.observer.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementa un ConcreteObserver del patrón Observer que procesa eventos
 * de dominio y envía notificaciones mediante los canales disponibles.
 */
public class GestorNotificaciones implements Subscriber {

    private static final Logger log = LoggerFactory.getLogger(GestorNotificaciones.class);

    private final NotifierFactory factory;

    /**
     * Inicializa el gestor con la fábrica de notificadores.
     *
     * @param factory fábrica encargada de crear los notificadores por canal
     * @throws IllegalArgumentException si la fábrica de notificadores es nula
     */
    public GestorNotificaciones(NotifierFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("La fábrica de notificadores es obligatoria.");
        }
        this.factory = factory;
    }

    /**
     * Procesa un evento de dominio y solicita el envío de una notificación
     * por cada canal disponible, registrando los errores de envío.
     *
     * @param event evento de dominio que se desea notificar
     */
    @Override
    public void onEvent(DomainEvent event) {
        String mensaje = formatearMensaje(event);
        for (Canal canal : Canal.values()) {
            try {
                Notifier notifier = factory.crearNotifier(canal);
                notifier.enviar(mensaje, canal.name());
            } catch (Exception e) {
                log.warn("Error enviando notificación por {}: {}", canal, e.getMessage());
            }
        }
    }

    /**
     * Genera un mensaje descriptivo según el tipo de evento recibido.
     *
     * @param event evento de dominio que se desea formatear
     * @return mensaje correspondiente al evento o descripción genérica
     *         si no existe un formato específico
     * @throws NullPointerException si el evento es nulo
     */
    private String formatearMensaje(DomainEvent event) {
        if (event instanceof ScrimStateChanged sce) {
            return "Scrim " + sce.scrimId() + " cambió de " + sce.estadoAnterior() + " a " + sce.nuevoEstado();
        }
        if (event instanceof ScrimCreadoEvent sce) {
            return "Nuevo scrim creado: " + sce.scrimId();
        }
        if (event instanceof LobbyArmadoEvent lae) {
            return "Lobby completo para scrim: " + lae.scrimId();
        }
        return "Evento de dominio: " + event.getClass().getSimpleName();
    }

    /**
     * Obtiene la fábrica de notificadores utilizada por el gestor.
     *
     * @return fábrica de notificadores configurada
     */
    public NotifierFactory getFactory() { return factory; }
}
