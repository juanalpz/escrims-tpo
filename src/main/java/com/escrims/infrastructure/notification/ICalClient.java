package com.escrims.infrastructure.notification;

/**
 * Implementa el Adaptee del patrón Adapter, representando un cliente
 * de iCalendar para la creación y sincronización de eventos de calendario.
 */
public class ICalClient {

    /**
     * Simula la creación de un evento en un calendario externo.
     *
     * @param titulo título del evento que se desea crear
     * @param fecha fecha del evento de calendario
     * @param duracion duración prevista del evento
     * @return true como resultado simulado de una creación exitosa
     */
    public boolean crearEvento(String titulo, String fecha, String duracion) {
        // Integración real con iCal (stub)
        return true;
    }
}
