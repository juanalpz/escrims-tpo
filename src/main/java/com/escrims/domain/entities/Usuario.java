package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Jugador de la plataforma. Contiene los datos de perfil, matchmaking,
 * historial de fair play y sanciones.
 */
public class Usuario {

    /** Puntuación máxima de fair play que otorgan los feedbacks. */
    public static final double RATING_MAXIMO = 5.0;

    private final UUID id;
    private String username;
    private boolean emailVerificado;
    private int mmr;
    private int latenciaPromedio;
    private Region region;
    private final Set<RolJuego> rolesPreferidos = EnumSet.noneOf(RolJuego.class);
    private final Map<Juego, Rango> rangosPorJuego = new EnumMap<>(Juego.class);
    private final List<Sancion> sanciones = new ArrayList<>();
    private int scrimsJugados;
    private int abandonos;
    private double ratingFairPlay = RATING_MAXIMO;

    /**
     * Constructor completo para matchmaking y uso general.
     */
    public Usuario(String username, int mmr, int latenciaPromedio, Region region) {
        this.id = UUID.randomUUID();
        this.username = Objects.requireNonNull(username, "username");
        this.mmr = mmr;
        this.latenciaPromedio = latenciaPromedio;
        this.region = region;
    }

    /**
     * Constructor mínimo para tests del Builder y creación rápida.
     * Genera un username basado en el UUID.
     */
    public Usuario() {
        this.id = UUID.randomUUID();
        this.username = "usuario-" + id.toString().substring(0, 8);
    }

    // ── Identidad ──────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    // ── Verificación de email (RN-01) ──────────────────────────

    public boolean isEmailVerificado() {
        return emailVerificado;
    }

    public void setEmailVerificado(boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
    }

    // ── Sanciones y cooldowns (RN-06) ──────────────────────────

    /**
     * Verifica si el usuario tiene alguna sanción con cooldown vigente
     * en este momento. Delegación a {@link #tieneSancionActiva(LocalDateTime)}.
     */
    public boolean tieneCooldownActivo() {
        return tieneSancionActiva(LocalDateTime.now());
    }

    public boolean tieneSancionActiva(LocalDateTime momento) {
        return sanciones.stream().anyMatch(s -> s.estaActiva(momento));
    }

    public List<Sancion> getSanciones() {
        return Collections.unmodifiableList(sanciones);
    }

    public void agregarSancion(Sancion sancion) {
        sanciones.add(Objects.requireNonNull(sancion, "sancion"));
    }

    public int getStrikes() {
        return sanciones.stream().mapToInt(Sancion::getStrikes).sum();
    }

    // ── MMR y matchmaking ──────────────────────────────────────

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

    // ── Rangos por Juego (RN-03 y perfil de usuario) ───────────

    public Rango getRangoParaJuego(Juego juego) {
        if (juego == null) {
            return null;
        }
        Rango rango = rangosPorJuego.get(juego);
        if (rango == null && mmr > 0) {
            try {
                return Rango.desdeMMR(mmr);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return rango;
    }

    public void asignarRango(Juego juego, Rango rango) {
        if (juego == null) {
            throw new IllegalArgumentException("El juego no puede ser nulo.");
        }
        if (rango == null) {
            rangosPorJuego.remove(juego);
        } else {
            rangosPorJuego.put(juego, rango);
        }
    }

    public Map<Juego, Rango> getRangosPorJuego() {
        return Collections.unmodifiableMap(rangosPorJuego);
    }

    // ── Historial y fair play ──────────────────────────────────

    public int getScrimsJugados() {
        return scrimsJugados;
    }

    public int getAbandonos() {
        return abandonos;
    }

    /** Registra el historial de participación. */
    public void registrarHistorial(int scrimsJugados, int abandonos) {
        if (scrimsJugados < 0 || abandonos < 0 || abandonos > scrimsJugados) {
            throw new IllegalArgumentException("Historial inválido");
        }
        this.scrimsJugados = scrimsJugados;
        this.abandonos = abandonos;
    }

    /** Proporción de scrims abandonados, entre 0 y 1. Sin historial devuelve 0. */
    public double getTasaAbandono() {
        return scrimsJugados == 0 ? 0.0 : (double) abandonos / scrimsJugados;
    }

    /** Rating de fair play, entre 0 y {@link #RATING_MAXIMO}. */
    public double getRatingFairPlay() {
        return ratingFairPlay;
    }

    public void setRatingFairPlay(double ratingFairPlay) {
        if (ratingFairPlay < 0 || ratingFairPlay > RATING_MAXIMO) {
            throw new IllegalArgumentException("El rating debe estar entre 0 y " + RATING_MAXIMO);
        }
        this.ratingFairPlay = ratingFairPlay;
    }

    // ── equals / hashCode / toString ───────────────────────────

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
