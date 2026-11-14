package com.escrims.domain.notification;

/**
 * «Abstract Product» Contrato comun de todos los canales de notificacion
 * Tambien es el «Target» del patron Adapter: los adaptadores de proveedores
 * externos (Discord, SendGrid, Firebase) lo implementan
 */
public interface Notifier {

    /** Canal que atiende este notifier. */
    Canal canal();

    /**
     * Envía la notificación y actualiza su estado (ENVIADA o FALLIDA).
     *
     * @throws IllegalArgumentException si la notificación es de otro canal
     * @throws NotificacionException    si el proveedor rechaza o falla el envío
     */
    void send(Notificacion notificacion);
}
