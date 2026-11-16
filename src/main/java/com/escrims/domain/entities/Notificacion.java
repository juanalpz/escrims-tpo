package com.escrims.domain.entities;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa una notificación del sistema, incluyendo su canal,
 * mensaje y estado de envío.
 */
public class Notificacion {

    private final UUID id;
    private final Canal canal;
    private EstadoNotificacion estado;
    private final String mensaje;

    /**
     * Crea una notificación con estado inicial pendiente.
     *
     * @param canal canal utilizado para enviar la notificación
     * @param mensaje contenido de la notificación
     * @throws IllegalArgumentException si el canal o el mensaje son nulos
     */
    public Notificacion(Canal canal, String mensaje) {
        if (canal == null || mensaje == null) {
            throw new IllegalArgumentException("Canal y mensaje son obligatorios.");
        }
        this.id = UUID.randomUUID();
        this.canal = canal;
        this.mensaje = mensaje;
        this.estado = EstadoNotificacion.PENDIENTE;
    }

    /**
     * Actualiza el estado de la notificación a enviada.
     */
    public void marcarEnviada() {
        this.estado = EstadoNotificacion.ENVIADA;
    }

    /**
     * Actualiza el estado de la notificación a fallida.
     */
    public void marcarFallida() {
        this.estado = EstadoNotificacion.FALLIDA;
    }

    /**
     * Obtiene el identificador único de la notificación.
     *
     * @return identificador de la notificación
     */
    public UUID getId() { return id; }

    /**
     * Obtiene el canal de envío de la notificación.
     *
     * @return canal de notificación
     */
    public Canal getCanal() { return canal; }

    /**
     * Obtiene el estado actual de la notificación.
     *
     * @return estado de envío
     */
    public EstadoNotificacion getEstado() { return estado; }

    /**
     * Obtiene el contenido de la notificación.
     *
     * @return mensaje de la notificación
     */
    public String getMensaje() { return mensaje; }

    /**
     * Compara dos notificaciones según su identificador único.
     *
     * @param o objeto con el que se realiza la comparación
     * @return true si ambas notificaciones tienen el mismo identificador;
     *         false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Notificacion n && id.equals(n.id));
    }

    /**
     * Calcula el código hash a partir del identificador único.
     *
     * @return código hash de la notificación
     */
    @Override
    public int hashCode() { return Objects.hash(id); }
}
