package com.escrims.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Jugador de la plataforma. Por ahora solo contiene los datos que necesita el
 * emparejamiento; el resto del perfil (email, passwordHash, rol de sistema...)
 * se agrega con el módulo de usuarios.
 */
public class Usuario {

    /** Puntuación máxima de fair play que otorgan los feedbacks. */
    public static final double RATING_MAXIMO = 5.0;

    private final UUID id;
    private final String username;
    private int mmr;
    private int latenciaPromedio;
    private Region region;
    private final Set<RolJuego> rolesPreferidos = EnumSet.noneOf(RolJuego.class);
    private final List<Sancion> sanciones = new ArrayList<>();
    private int scrimsJugados;
    private int abandonos;
    private double ratingFairPlay = RATING_MAXIMO;

    public Usuario(String username, int mmr, int latenciaPromedio, Region region) {
        this.id = UUID.randomUUID();
        this.username = Objects.requireNonNull(username, "username");
        this.mmr = mmr;
        this.latenciaPromedio = latenciaPromedio;
        this.region = region;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getMmr() {
        return mmr;
    }

    public void setMmr(int mmr) {
        this.mmr = mmr;
    }

    /** Ping promedio en milisegundos. */
    public int getLatenciaPromedio() {
        return latenciaPromedio;
    }

    public void setLatenciaPromedio(int latenciaPromedio) {
        this.latenciaPromedio = latenciaPromedio;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Set<RolJuego> getRolesPreferidos() {
        return Collections.unmodifiableSet(rolesPreferidos);
    }

    public void agregarRolPreferido(RolJuego rol) {
        rolesPreferidos.add(rol);
    }

    public List<Sancion> getSanciones() {
        return Collections.unmodifiableList(sanciones);
    }

    public void agregarSancion(Sancion sancion) {
        sanciones.add(sancion);
    }

    public int getStrikes() {
        return sanciones.stream().mapToInt(Sancion::getStrikes).sum();
    }

    public boolean tieneSancionActiva(LocalDateTime momento) {
        return sanciones.stream().anyMatch(s -> s.estaActiva(momento));
    }

    public int getScrimsJugados() {
        return scrimsJugados;
    }

    public int getAbandonos() {
        return abandonos;
    }

    /** Registra el historial de participación: scrims jugados y cuántos abandonó (o no-show). */
    public void registrarHistorial(int scrimsJugados, int abandonos) {
        if (scrimsJugados < 0 || abandonos < 0 || abandonos > scrimsJugados) {
            throw new IllegalArgumentException("Historial inválido");
        }
        this.scrimsJugados = scrimsJugados;
        this.abandonos = abandonos;
    }

    /** Proporción de scrims abandonados, entre 0 y 1. Un jugador sin historial tiene 0. */
    public double getTasaAbandono() {
        return scrimsJugados == 0 ? 0.0 : (double) abandonos / scrimsJugados;
    }

    /** Promedio de las puntuaciones de fair play recibidas, entre 0 y {@link #RATING_MAXIMO}. */
    public double getRatingFairPlay() {
        return ratingFairPlay;
    }

    public void setRatingFairPlay(double ratingFairPlay) {
        if (ratingFairPlay < 0 || ratingFairPlay > RATING_MAXIMO) {
            throw new IllegalArgumentException("El rating debe estar entre 0 y " + RATING_MAXIMO);
        }
        this.ratingFairPlay = ratingFairPlay;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Usuario otro && id.equals(otro.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return username;
    }
}
