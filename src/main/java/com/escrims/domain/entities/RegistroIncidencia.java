package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Asienta un abandono o no-show de un jugador en un scrim, junto con
 * la sanción que el sistema de strikes le aplicó por esa incidencia.
 */
public class RegistroIncidencia {

    private final UUID id;
    private final Scrim scrim;
    private final Usuario usuario;
    private final TipoInfraccion tipo;
    private final String detalle;
    private final Sancion sancion;
    private final LocalDateTime fecha;

    /**
     * Crea el registro de una incidencia de participación.
     *
     * @param scrim scrim en el que ocurrió la incidencia
     * @param usuario jugador que abandonó o no se presentó
     * @param tipo tipo de incidencia ({@link TipoInfraccion#ABANDONO} o {@link TipoInfraccion#NO_SHOW})
     * @param detalle observación opcional de quien la registró
     * @param sancion sanción aplicada por la incidencia
     * @throws IllegalArgumentException si falta el scrim, el usuario o la sanción,
     *         o si el tipo no corresponde a un abandono o no-show
     */
    public RegistroIncidencia(Scrim scrim, Usuario usuario, TipoInfraccion tipo, String detalle, Sancion sancion) {
        if (scrim == null || usuario == null || sancion == null) {
            throw new IllegalArgumentException("El scrim, el usuario y la sanción son obligatorios.");
        }
        if (tipo != TipoInfraccion.ABANDONO && tipo != TipoInfraccion.NO_SHOW) {
            throw new IllegalArgumentException("Solo se registran incidencias de tipo ABANDONO o NO_SHOW.");
        }
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.usuario = usuario;
        this.tipo = tipo;
        this.detalle = detalle;
        this.sancion = sancion;
        this.fecha = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Scrim getScrim() { return scrim; }
    public Usuario getUsuario() { return usuario; }
    public TipoInfraccion getTipo() { return tipo; }
    public String getDetalle() { return detalle; }
    public Sancion getSancion() { return sancion; }
    public LocalDateTime getFecha() { return fecha; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof RegistroIncidencia r && id.equals(r.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
