package com.escrims.domain.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Scrim con los datos que necesita el emparejamiento. El ciclo de vida
 * (patrón State) se agrega en el módulo de estados.
 */
public class Scrim {

    private final UUID id;
    private final Juego juego;
    private final Formato formato;
    private final Region region;
    private final Rango rango;
    private final int latenciaMaxima;
    private final Set<Usuario> jugadores = new LinkedHashSet<>();
    private final Set<RolJuego> rolesBuscados = EnumSet.noneOf(RolJuego.class);

    /**
     * @param rango          límites de MMR, o {@code null} si admite cualquier nivel
     * @param latenciaMaxima ping máximo en ms, o 0 si no tiene límite
     */
    public Scrim(Juego juego, Formato formato, Region region, Rango rango, int latenciaMaxima) {
        this.id = UUID.randomUUID();
        this.juego = juego;
        this.formato = Objects.requireNonNull(formato, "formato");
        this.region = region;
        this.rango = rango;
        this.latenciaMaxima = latenciaMaxima;
    }

    public UUID getId() {
        return id;
    }

    public Juego getJuego() {
        return juego;
    }

    public Formato getFormato() {
        return formato;
    }

    public Region getRegion() {
        return region;
    }

    public Rango getRango() {
        return rango;
    }

    public int getLatenciaMaxima() {
        return latenciaMaxima;
    }

    public Set<Usuario> getJugadores() {
        return Collections.unmodifiableSet(jugadores);
    }

    public void agregarJugador(Usuario usuario) {
        if (cuposDisponibles() == 0) {
            throw new IllegalStateException("El scrim no tiene cupos disponibles");
        }
        jugadores.add(usuario);
    }

    public int cuposDisponibles() {
        return formato.getCuposTotales() - jugadores.size();
    }

    /** Roles que el scrim todavía necesita cubrir. Vacío si no exige roles. */
    public Set<RolJuego> getRolesBuscados() {
        return Collections.unmodifiableSet(rolesBuscados);
    }

    public void buscarRol(RolJuego rol) {
        rolesBuscados.add(rol);
    }
}
