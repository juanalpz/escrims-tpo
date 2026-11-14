package com.escrims.domain.notification;

/** Error al entregar una notificacion a traves de un proveedor externo */
public class NotificacionException extends RuntimeException {

    public NotificacionException(String mensaje) {
        super(mensaje);
    }

    public NotificacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
