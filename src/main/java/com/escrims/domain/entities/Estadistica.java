package com.escrims.domain.entities;

import java.util.Objects;
import java.util.UUID;

/**
 * Almacena el rendimiento individual (MVP, KDA y observaciones) de un jugador al finalizar el Scrim.
 */
public class Estadistica {

    private final UUID id;
    private final Scrim scrim;
    private final Usuario usuario;
    private final int kills;
    private final int deaths;
    private final int assists;
    private boolean mvp;
    private String observaciones;

    public Estadistica(Scrim scrim, Usuario usuario, int kills, int deaths, int assists, boolean mvp, String observaciones) {
        if (scrim == null || usuario == null) {
            throw new IllegalArgumentException("El scrim y el usuario son obligatorios.");
        }
        if (kills < 0 || deaths < 0 || assists < 0) {
            throw new IllegalArgumentException("Los valores de kills, deaths y assists no pueden ser negativos.");
        }
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.usuario = usuario;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.mvp = mvp;
        this.observaciones = observaciones;
    }

    /**
     * Calcula el ratio KDA evitando división por cero si deaths == 0.
     */
    public double calcularRatioKDA() {
        return (double) (this.kills + this.assists) / Math.max(1, this.deaths);
    }

    /**
     * Devuelve el KDA formateado (ej. "14/3/8").
     */
    public String getKdaFormateado() {
        return this.kills + "/" + this.deaths + "/" + this.assists;
    }

    public UUID getId() { return id; }
    public Scrim getScrim() { return scrim; }
    public Usuario getUsuario() { return usuario; }
    public boolean isMvp() { return mvp; }
    public void setMvp(boolean mvp) { this.mvp = mvp; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getAssists() { return assists; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Estadistica that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
