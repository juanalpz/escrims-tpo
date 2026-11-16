package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.ResultadoEnvio;

/**
 * Implementa el patrón Adapter para integrar {@link ICalClient}
 * con el contrato de notificaciones {@link Notifier}.
 */
public class ICalAdapter implements Notifier {

    private final ICalClient cliente;

    /**
     * Inicializa el adaptador con el cliente de iCalendar.
     *
     * @param cliente cliente encargado de crear eventos de calendario
     */
    public ICalAdapter(ICalClient cliente) {
        this.cliente = cliente;
    }

    /**
     * Solicita la creación de un evento de calendario mediante iCalendar,
     * utilizando una duración fija de 60 minutos.
     *
     * @param mensaje descripción del evento que se desea crear
     * @param destinatario destinatario del evento de calendario
     * @return resultado de la creación con un intento registrado,
     *         indicando si la operación fue exitosa
     */
    @Override
    public ResultadoEnvio enviar(String mensaje, String destinatario) {
        boolean ok = cliente.crearEvento(mensaje, destinatario, "60");
        return ok ? ResultadoEnvio.exito(1) : ResultadoEnvio.fallo(1);
    }

    /**
     * Devuelve un resultado exitoso simulado sin realizar nuevos intentos
     * de creación del evento.
     *
     * @param maxIntentos cantidad máxima de reintentos solicitados
     * @return resultado exitoso con un intento registrado
     */
    @Override
    public ResultadoEnvio reintentar(int maxIntentos) {
        return ResultadoEnvio.exito(1);
    }
}
