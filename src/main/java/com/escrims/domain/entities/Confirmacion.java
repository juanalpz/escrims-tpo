package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Registra la confirmación de asistencia de un jugador seleccionado en un lobby armado (CU6, RN-09).
 */
public class Confirmacion {

    private final UUID id;
    private final Usuario usuario;
    private final Scrim scrim;
    private boolean confirmado;
    private LocalDateTime fechaConfirmacion;

    /**
     * Crea una solicitud de confirmación pendiente para un participante del scrim.
     *
     * @param usuario jugador que debe confirmar su asistencia.
     * @param scrim   encuentro para el cual se solicita la confirmación.
     * @throws IllegalArgumentException si el usuario o el scrim son nulos.
     */
    public Confirmacion(Usuario usuario, Scrim scrim) {
        if (usuario == null || scrim == null) {
            throw new IllegalArgumentException("El usuario y el scrim son obligatorios.");
        }
        this.id = UUID.randomUUID();
        this.usuario = usuario;
        this.scrim = scrim;
        this.confirmado = false;
    }

    /**
     * Marca la asistencia como confirmada y registra la fecha y hora exacta de la operación (RN-09).
     *
     * @throws IllegalStateException si el jugador ya había confirmado previamente su participación.
     */
    public void confirmar() {
        if (this.confirmado) {
            throw new IllegalStateException("El usuario ya confirmó su participación en este scrim.");
        }
        this.confirmado = true;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Scrim getScrim() { return scrim; }
    public boolean isConfirmado() { return confirmado; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Confirmacion that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
