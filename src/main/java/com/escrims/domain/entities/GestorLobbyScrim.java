package com.escrims.domain.entities;

import java.util.*;

/**
 * Administra la distribución de titulares en Equipo A / Equipo B
 * y la lista de espera (suplentes) FIFO de un Scrim (RN-05, RN-10, RN-17).
 */
public class GestorLobbyScrim {

    private final Scrim scrim;
    private final Equipo equipoA;
    private final Equipo equipoB;
    private final LinkedHashMap<Usuario, RolJuego> listaEspera; // Orden FIFO sin duplicados

    public GestorLobbyScrim(Scrim scrim) {
        if (scrim == null) {
            throw new IllegalArgumentException("El Scrim es obligatorio.");
        }
        this.scrim = scrim;
        int porLado = scrim.getFormato().getJugadoresPorLado();
        this.equipoA = new Equipo(LadoEquipo.EQUIPO_A, porLado);
        this.equipoB = new Equipo(LadoEquipo.EQUIPO_B, porLado);
        this.listaEspera = new LinkedHashMap<>();
    }

    /**
     * Incorpora un jugador como titular si hay lugar en Equipo A o Equipo B;
     * si ambos están completos, lo encola en la lista de espera (suplentes).
     * 
     * @return true si entró como titular, false si quedó en lista de espera.
     */
    public boolean incorporarJugador(Usuario usuario, RolJuego rol) {
        if (usuario == null || rol == null) {
            throw new IllegalArgumentException("Usuario y rol son obligatorios.");
        }
        if (esTitular(usuario) || estaEnListaEspera(usuario)) {
            throw new IllegalStateException("El jugador ya forma parte del lobby o de la lista de espera.");
        }

        if (!equipoA.estaCompleto()) {
            return equipoA.agregarJugador(usuario, rol);
        }
        if (!equipoB.estaCompleto()) {
            return equipoB.agregarJugador(usuario, rol);
        }

        listaEspera.put(usuario, rol);
        return false;
    }

    /**
     * Incorpora un jugador directamente a un bando específico (soporte para
     * InvitarJugadorCommand / undo).
     */
    public boolean incorporarAEquipo(Usuario usuario, RolJuego rol, LadoEquipo lado) {
        if (usuario == null || rol == null || lado == null) {
            throw new IllegalArgumentException("Usuario, rol y lado son obligatorios.");
        }
        if (esTitular(usuario)) {
            throw new IllegalStateException("El jugador ya es titular en un equipo.");
        }
        listaEspera.remove(usuario);
        Equipo destino = (lado == LadoEquipo.EQUIPO_A) ? equipoA : equipoB;
        return destino.agregarJugador(usuario, rol);
    }

    /**
     * Da de baja a un titular y promueve automáticamente al primer suplente FIFO
     * (RN-10 / RN-17).
     */
    public Optional<Usuario> darDeBajaYPromoverSuplente(Usuario titularSaliente) {
        Equipo equipoAfectado = obtenerEquipoDe(titularSaliente)
                .orElseThrow(() -> new IllegalStateException("El usuario no es titular en ningún equipo."));

        equipoAfectado.removerJugador(titularSaliente);

        if (!listaEspera.isEmpty()) {
            Map.Entry<Usuario, RolJuego> siguiente = listaEspera.entrySet().iterator().next();
            Usuario suplente = siguiente.getKey();
            RolJuego rolSuplente = siguiente.getValue();

            listaEspera.remove(suplente);
            equipoAfectado.agregarJugador(suplente, rolSuplente);
            return Optional.of(suplente);
        }

        return Optional.empty();
    }

    /**
     * Intercambia dos jugadores entre Equipo A y Equipo B conservando sus roles
     * (soporte para SwapJugadoresCommand).
     */
    public void intercambiarJugadores(Usuario jugador1, Usuario jugador2) {
        Equipo eq1 = obtenerEquipoDe(jugador1)
                .orElseThrow(() -> new IllegalStateException("El primer jugador no es titular."));
        Equipo eq2 = obtenerEquipoDe(jugador2)
                .orElseThrow(() -> new IllegalStateException("El segundo jugador no es titular."));

        if (eq1.equals(eq2)) {
            throw new IllegalStateException("Ambos jugadores ya pertenecen al mismo equipo.");
        }

        RolJuego rol1 = eq1.getRolDe(jugador1);
        RolJuego rol2 = eq2.getRolDe(jugador2);

        eq1.removerJugador(jugador1);
        eq2.removerJugador(jugador2);

        eq1.agregarJugador(jugador2, rol2);
        eq2.agregarJugador(jugador1, rol1);
    }

    public boolean esTitular(Usuario usuario) {
        return equipoA.contieneJugador(usuario) || equipoB.contieneJugador(usuario);
    }

    public boolean estaEnListaEspera(Usuario usuario) {
        return listaEspera.containsKey(usuario);
    }

    public Optional<Equipo> obtenerEquipoDe(Usuario usuario) {
        if (equipoA.contieneJugador(usuario))
            return Optional.of(equipoA);
        if (equipoB.contieneJugador(usuario))
            return Optional.of(equipoB);
        return Optional.empty();
    }

    public boolean lobbyCompleto() {
        return equipoA.estaCompleto() && equipoB.estaCompleto();
    }

    public Equipo getEquipoA() {
        return equipoA;
    }

    public Equipo getEquipoB() {
        return equipoB;
    }

    public List<Usuario> getListaEsperaOrdenada() {
        return List.copyOf(listaEspera.keySet());
    }

    public Scrim getScrim() {
        return scrim;
    }
}