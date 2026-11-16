package com.escrims.infrastructure.notification;

import com.escrims.domain.observer.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestiona la programación de recordatorios de scrims en calendarios externos,
 * delegando la sincronización a una implementación de CalendarSyncTarget.
 */
public class CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);

    private final CalendarSyncTarget sync;

    /**
     * Inicializa el servicio con el componente de sincronización de calendarios.
     *
     * @param sync adaptador encargado de sincronizar eventos con calendarios externos
     * @throws IllegalArgumentException si el componente de sincronización es nulo
     */
    public CalendarService(CalendarSyncTarget sync) {
        if (sync == null) {
            throw new IllegalArgumentException("CalendarSyncTarget es obligatorio.");
        }
        this.sync = sync;
    }

    /**
     * Solicita la sincronización de un evento para un recordatorio,
     * registrando las horas de anticipación indicadas.
     *
     * @param evento evento de dominio que se desea sincronizar
     * @param horasAntes cantidad de horas de anticipación del recordatorio
     * @throws IllegalArgumentException si las horas de anticipación
     *         son menores o iguales a cero
     * @throws NullPointerException si el evento es nulo
     */
    public void programarRecordatorio(DomainEvent evento, int horasAntes) {
        if (horasAntes <= 0) {
            throw new IllegalArgumentException("Las horas de anticipación deben ser mayores a 0.");
        }
        log.info("Programando recordatorio {} horas antes del evento: {}", horasAntes, evento.getClass().getSimpleName());
        sync.sincronizar(evento);
    }

    /**
     * Obtiene el componente de sincronización de calendarios.
     *
     * @return adaptador de sincronización configurado
     */
    public CalendarSyncTarget getSync() { return sync; }
}
