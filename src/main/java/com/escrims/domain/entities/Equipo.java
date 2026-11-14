package com.escrims.domain.entities;

import java.util.*;

/**
 * Representa uno de los dos bandos (Equipo A o B) dentro de un Scrim.
 * Usa {@code Set<Usuario>} para garantizar que no haya jugadores duplicados en el mismo equipo.
 */
public class Equipo {

    private final UUID id;
    private final LadoEquipo lado;
    private final int cupoMaximo;
    private final Set<Usuario> jugadores;
    private final Map<Usuario, RolJuego> rolesAsignados;

    public Equipo(LadoEquipo lado, int cupoMaximo) {
        if (lado == null) {
            throw new IllegalArgumentException("El lado del equipo es obligatorio.");
        }
        if (cupoMaximo <= 0) {
            throw new IllegalArgumentException("El cupo máximo del equipo debe ser mayor a 0.");
        }
        this.id = UUID.randomUUID();
        this.lado = lado;
        this.cupoMaximo = cupoMaximo;
        this.jugadores = new HashSet<>();
        this.rolesAsignados = new HashMap<>();
    }

    /**
     * Agrega un jugador al equipo validando que no esté lleno ni duplicado.
     */
    public boolean agregarJugador(Usuario usuario, RolJuego rol) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        if (this.jugadores.size() >= this.cupoMaximo) {
            throw new IllegalStateException("El equipo " + this.lado + " ya alcanzó su cupo máximo.");
        }
        boolean agregado = this.jugadores.add(usuario);
        if (agregado && rol != null) {
            this.rolesAsignados.put(usuario, rol);
        }
        return agregado;
    }

    /**
     * Quita un jugador del equipo y limpia su rol asignado.
     */
    public boolean removerJugador(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        this.rolesAsignados.remove(usuario);
        return this.jugadores.remove(usuario);
    }

    /**
     * Permite reasignar el rol de un jugador ya presente en el equipo (usado por AsignarRolCommand).
     */
    public void asignarRol(Usuario usuario, RolJuego nuevoRol) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        if (nuevoRol == null) {
            throw new IllegalArgumentException("El nuevo rol no puede ser nulo.");
        }
        if (!this.jugadores.contains(usuario)) {
            throw new IllegalStateException("El jugador no pertenece a este equipo.");
        }
        this.rolesAsignados.put(usuario, nuevoRol);
    }

    public boolean estaCompleto() {
        return this.jugadores.size() == this.cupoMaximo;
    }

    public UUID getId() { return id; }
    public LadoEquipo getLado() { return lado; }
    public int getCupoMaximo() { return cupoMaximo; }
    public Set<Usuario> getJugadores() { return Collections.unmodifiableSet(jugadores); }
    public Map<Usuario, RolJuego> getRolesAsignados() { return Collections.unmodifiableMap(rolesAsignados); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipo equipo)) return false;
        return Objects.equals(id, equipo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
