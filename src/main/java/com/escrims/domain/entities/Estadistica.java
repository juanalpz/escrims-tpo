package com.escrims.domain.entities;

import java.util.Objects;
import java.util.UUID;

/**
 * Almacena el rendimiento individual (KDA, MVP y observaciones) de un jugador al finalizar un scrim (CU8, RN-13).
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

    /**
     * Inicializa el registro de estadísticas individuales de un jugador para un scrim finalizado.
     *
     * @param scrim         scrim finalizado al que pertenece la métrica.
     * @param usuario       jugador evaluado.
     * @param kills         cantidad de asesinatos o bajas logradas.
     * @param deaths        cantidad de muertes sufridas.
     * @param assists       cantidad de asistencias realizadas.
     * @param mvp           indica si el jugador fue seleccionado como MVP de la partida.
     * @param observaciones comentarios o notas adicionales sobre su desempeño.
     * @throws IllegalArgumentException si el scrim o el usuario son nulos, o si alguna métrica es negativa.
     */
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
     * Calcula el ratio numérico KDA evitando la división por cero cuando las muertes son 0.
     *
     * @return valor decimal correspondiente a {@code (kills + assists) / max(1, deaths)}.
     */
    public double calcularRatioKDA() {
        return (double) (this.kills + this.assists) / Math.max(1, this.deaths);
    }

    /**
     * Devuelve el registro KDA formateado en cadena de texto estándar (ej. {@code "14/3/8"}).
     *
     * @return representación textual {@code kills/deaths/assists}.
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
