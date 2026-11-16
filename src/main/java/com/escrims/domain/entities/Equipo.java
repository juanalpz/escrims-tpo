package com.escrims.domain.entities;

import java.util.*;

/**
 * Representa uno de los dos bandos (Equipo A o Equipo B) dentro de un scrim,
 * garantizando que no existan jugadores duplicados y administrando sus roles asignados.
 */
public class Equipo {

    private final UUID id;
    private final LadoEquipo lado;
    private final int cupoMaximo;
    private final Set<Usuario> jugadores;
    private final Map<Usuario, RolJuego> rolesAsignados;

    /**
     * Inicializa un equipo indicando su bando y la cantidad máxima de integrantes permitidos.
     *
     * @param lado       bando del equipo ({@link LadoEquipo#EQUIPO_A} o {@link LadoEquipo#EQUIPO_B}).
     * @param cupoMaximo cantidad máxima de jugadores que puede albergar el equipo.
     * @throws IllegalArgumentException si el lado es nulo o el cupo máximo es menor o igual a cero.
     */
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
     * Incorpora un jugador al equipo con su rol correspondiente si hay cupo disponible (RN-05).
     *
     * @param usuario jugador a incorporar en el equipo.
     * @param rol     rol dentro del juego asignado al participante.
     * @return {@code true} si el jugador fue agregado, o {@code false} si ya pertenecía al equipo.
     * @throws IllegalArgumentException si el usuario recibido es nulo.
     * @throws IllegalStateException    si el equipo ya alcanzó su cupo máximo.
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
     * Quita a un jugador del equipo y elimina su asignación de rol.
     *
     * @param usuario jugador a remover del equipo.
     * @return {@code true} si el jugador estaba presente y fue eliminado, o {@code false} en caso contrario.
     * @throws IllegalArgumentException si el usuario recibido es nulo.
     */
    public boolean removerJugador(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        this.rolesAsignados.remove(usuario);
        return this.jugadores.remove(usuario);
    }

    /**
     * Reasigna el rol de un jugador que ya forma parte del equipo (RN-12).
     *
     * @param usuario  jugador integrante del equipo.
     * @param nuevoRol nuevo rol dentro del juego a establecer.
     * @throws IllegalArgumentException si el usuario o el rol son nulos.
     * @throws IllegalStateException    si el usuario no pertenece al equipo.
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

    /**
     * Indica si el equipo alcanzó su capacidad máxima de jugadores.
     *
     * @return {@code true} si la cantidad de integrantes es igual al cupo máximo, o {@code false} en caso contrario.
     */
    public boolean estaCompleto() {
        return this.jugadores.size() == this.cupoMaximo;
    }

    /**
     * Obtiene el rol asignado a un jugador dentro del equipo.
     *
     * @param usuario jugador consultado.
     * @return rol asignado al jugador, o {@code null} si no tiene rol o no pertenece al equipo.
     */
    public RolJuego getRolDe(Usuario usuario) {
        return this.rolesAsignados.get(usuario);
    }

    /**
     * Verifica si un usuario forma parte del equipo.
     *
     * @param usuario usuario a consultar.
     * @return {@code true} si el usuario pertenece al equipo, o {@code false} en caso contrario.
     */
    public boolean contieneJugador(Usuario usuario) {
        return this.jugadores.contains(usuario);
    }

    /**
     * Intercambia los roles asignados entre dos integrantes del mismo equipo (RN-12).
     *
     * @param a primer jugador involucrado en el intercambio.
     * @param b segundo jugador involucrado en el intercambio.
     * @throws IllegalArgumentException si alguno de los usuarios recibidos es nulo.
     * @throws IllegalStateException    si alguno de los jugadores no pertenece al equipo.
     */
    public void intercambiar(Usuario a, Usuario b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Ambos usuarios son obligatorios.");
        }
        if (!this.jugadores.contains(a) || !this.jugadores.contains(b)) {
            throw new IllegalStateException("Ambos jugadores deben pertenecer al equipo.");
        }
        RolJuego rolA = this.rolesAsignados.get(a);
        RolJuego rolB = this.rolesAsignados.get(b);
        if (rolA != null) this.rolesAsignados.put(b, rolA);
        else this.rolesAsignados.remove(b);
        if (rolB != null) this.rolesAsignados.put(a, rolB);
        else this.rolesAsignados.remove(a);
    }

    public UUID getId() {
        return id;
    }

    public LadoEquipo getLado() {
        return lado;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public Set<Usuario> getJugadores() {
        return Collections.unmodifiableSet(jugadores);
    }

    public Map<Usuario, RolJuego> getRolesAsignados() {
        return Collections.unmodifiableMap(rolesAsignados);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Equipo equipo))
            return false;
        return Objects.equals(id, equipo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
