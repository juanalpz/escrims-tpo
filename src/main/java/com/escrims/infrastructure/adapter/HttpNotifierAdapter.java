package com.escrims.infrastructure.adapter;

import com.escrims.domain.notification.Notificacion;
import com.escrims.domain.notification.NotificacionException;
import com.escrims.domain.notification.Notifier;
import com.escrims.infrastructure.http.HttpTransport;

import java.util.Map;
import java.util.Objects;

/**
 * Base de los adaptadores de proveedores HTTP. Cada subclase solo traduce la
 * {@link Notificacion} del dominio al formato de la API externa; el envio, el
 * control de errores y el estado de la notificacion se resuelven aca
 */
abstract class HttpNotifierAdapter implements Notifier {

    private final HttpTransport transport;

    protected HttpNotifierAdapter(HttpTransport transport) {
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    /** Nombre del proveedor, para los mensajes de error. */
    protected abstract String proveedor();

    protected abstract String url();

    protected abstract Map<String, String> headers();

    protected abstract String cuerpo(Notificacion notificacion);

    @Override
    public final void send(Notificacion notificacion) {
        Objects.requireNonNull(notificacion, "notificacion");
        if (notificacion.getCanal() != canal()) {
            throw new IllegalArgumentException(
                    proveedor() + " sólo envía por " + canal() + ", no por " + notificacion.getCanal() + ".");
        }
        String cuerpo = cuerpo(notificacion);
        int status;
        try {
            status = transport.postJson(url(), headers(), cuerpo);
        } catch (RuntimeException e) {
            notificacion.marcarFallida();
            throw new NotificacionException("No se pudo conectar con " + proveedor() + ".", e);
        }
        if (status < 200 || status >= 300) {
            notificacion.marcarFallida();
            throw new NotificacionException(proveedor() + " respondió HTTP " + status + ".");
        }
        notificacion.marcarEnviada();
    }

    protected static String requerido(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta configurar " + nombre + ".");
        }
        return valor;
    }

    protected static String destinatario(Notificacion notificacion) {
        String destinatario = notificacion.getDestinatario();
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("La notificación no tiene destinatario.");
        }
        return destinatario;
    }
}
