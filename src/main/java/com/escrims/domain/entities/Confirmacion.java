package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Registra la confirmación de asistencia de un jugador seleccionado en el lobby armado.
 */
public class Confirmacion {

    private final UUID id;
    private final Usuario usuario;
    private final Scrim scrim;
    private boolean confirmado;
    private LocalDateTime fechaConfirmacion;

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
     * Marca la asistencia como confirmada y registra la marca de tiempo.
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
