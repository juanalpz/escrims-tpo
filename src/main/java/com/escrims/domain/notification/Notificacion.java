package com.escrims.domain.notification;

import java.util.Objects;
import java.util.UUID;

/**
 * Mensaje a entregar a un usuario por un canal concreto.
 * El {@code destinatario} depende del canal: direccion de email, token del
 * dispositivo o mencion de chat
 */
public class Notificacion {

    private final UUID id;
    private final String tipo;
    private final Canal canal;
    private final String destinatario;
    private final String titulo;
    private final String payload;
    private EstadoNotificacion estado;

    public Notificacion(String tipo, Canal canal, String destinatario, String titulo, String payload) {
        this.id = UUID.randomUUID();
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        this.canal = Objects.requireNonNull(canal, "canal");
        this.destinatario = destinatario;
        this.titulo = Objects.requireNonNull(titulo, "titulo");
        this.payload = Objects.requireNonNull(payload, "payload");
        this.estado = EstadoNotificacion.PENDIENTE;
    }

    public UUID getId() { return id; }
    public String getTipo() { return tipo; }
    public Canal getCanal() { return canal; }
    public String getDestinatario() { return destinatario; }
    public String getTitulo() { return titulo; }
    public String getPayload() { return payload; }
    public EstadoNotificacion getEstado() { return estado; }

    public void marcarEnviada() {
        this.estado = EstadoNotificacion.ENVIADA;
    }

    public void marcarFallida() {
        this.estado = EstadoNotificacion.FALLIDA;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Notificacion n && id.equals(n.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Notificacion[" + tipo + ", " + canal + ", " + destinatario + ", " + estado + "]";
    }
}
