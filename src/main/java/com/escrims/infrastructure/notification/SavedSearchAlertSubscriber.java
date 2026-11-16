package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.BusquedaGuardada;
import com.escrims.domain.entities.Canal;
import com.escrims.domain.observer.DomainEvent;
import com.escrims.domain.observer.ScrimCreadoEvent;
import com.escrims.domain.observer.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Implementa un ConcreteObserver del patrón Observer que procesa eventos
 * de creación de scrims para generar alertas de búsquedas guardadas.
 */
public class SavedSearchAlertSubscriber implements Subscriber {

    private static final Logger log = LoggerFactory.getLogger(SavedSearchAlertSubscriber.class);

    private final NotifierFactory factory;
    private final List<BusquedaGuardada> busquedasGuardadas;

    /**
     * Inicializa el suscriptor con la fábrica de notificadores
     * y las búsquedas guardadas que se deben procesar.
     *
     * @param factory fábrica encargada de crear los notificadores
     * @param busquedasGuardadas lista de búsquedas registradas,
     *                          o null para utilizar una lista vacía
     * @throws IllegalArgumentException si la fábrica de notificadores es nula
     */
    public SavedSearchAlertSubscriber(NotifierFactory factory, List<BusquedaGuardada> busquedasGuardadas) {
        if (factory == null) {
            throw new IllegalArgumentException("La fábrica de notificadores es obligatoria.");
        }
        this.factory = factory;
        this.busquedasGuardadas = busquedasGuardadas != null ? busquedasGuardadas : List.of();
    }

    /**
     * Procesa los eventos de creación de scrims y solicita una notificación
     * por cada búsqueda guardada registrada.
     *
     * @param event evento de dominio recibido por el suscriptor
     */
    @Override
    public void onEvent(DomainEvent event) {
        if (!(event instanceof ScrimCreadoEvent scrimCreado)) {
            return;
        }
        for (BusquedaGuardada busqueda : busquedasGuardadas) {
            notificar(busqueda, scrimCreado);
        }
    }

    /**
     * Solicita el envío de una alerta push asociada a una búsqueda guardada,
     * registrando cualquier excepción producida durante el envío.
     *
     * @param busqueda búsqueda guardada que origina la alerta
     * @param evento evento de creación del scrim que se desea notificar
     */
    private void notificar(BusquedaGuardada busqueda, ScrimCreadoEvent evento) {
        String mensaje = "¡Nuevo scrim disponible que coincide con tu búsqueda! Scrim ID: " + evento.scrimId();
        try {
            Notifier notifier = factory.crearNotifier(Canal.PUSH);
            notifier.enviar(mensaje, Canal.PUSH.name());
        } catch (Exception e) {
            log.warn("Error notificando alerta de búsqueda guardada: {}", e.getMessage());
        }
    }

    /**
     * Obtiene la fábrica de notificadores utilizada por el suscriptor.
     *
     * @return fábrica de notificadores configurada
     */
    public NotifierFactory getFactory() { return factory; }
}
