package com.escrims.infrastructure.notification;

import com.escrims.domain.notification.Canal;
import com.escrims.domain.notification.Notificacion;
import com.escrims.domain.notification.Notifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * «Concrete Product» de desarrollo: no llama a ningun proveedor, solo loguea
 * la notificacion y la guarda en memoria para poder inspeccionarla en tests
 */
public class LogNotifier implements Notifier {

    private static final Logger LOG = Logger.getLogger(LogNotifier.class.getName());

    private final Canal canal;
    private final List<Notificacion> enviadas = new ArrayList<>();

    public LogNotifier(Canal canal) {
        this.canal = Objects.requireNonNull(canal, "canal");
    }

    @Override
    public Canal canal() {
        return canal;
    }

    @Override
    public void send(Notificacion notificacion) {
        Objects.requireNonNull(notificacion, "notificacion");
        if (notificacion.getCanal() != canal) {
            throw new IllegalArgumentException(
                    "Este notifier sólo envía por " + canal + ", no por " + notificacion.getCanal() + ".");
        }
        LOG.info(() -> "[DEV][" + canal + "] -> " + notificacion.getDestinatario()
                + " | " + notificacion.getTitulo() + " | " + notificacion.getPayload());
        notificacion.marcarEnviada();
        enviadas.add(notificacion);
    }

    /** Notificaciones "enviadas" por este fake, en orden. */
    public List<Notificacion> getEnviadas() {
        return Collections.unmodifiableList(enviadas);
    }
}
