package com.escrims.domain.entities;

import java.util.*;

/**
 * Gestiona la asignación de jugadores a los equipos de un scrim
 * y la lista de espera de suplentes mediante el criterio FIFO (RN-05, RN-10, RN-17).
 */
public class GestorLobbyScrim {

    private final Scrim scrim;
    private final Equipo equipoA;
    private final Equipo equipoB;
    private final LinkedHashMap<Usuario, RolJuego> listaEspera;

    /**
     * Inicializa el lobby de un scrim con dos equipos y una lista de espera vacía.
     *
     * @param scrim scrim al que pertenece el lobby
     * @throws IllegalArgumentException si el scrim es nulo
     */
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
     * Incorpora un jugador al primer equipo disponible o a la lista de espera
     * si ambos equipos están completos (RN-05).
     *
     * @param usuario jugador que desea incorporarse
     * @param rol rol de juego asignado al jugador
     * @return true si ingresa como titular; false si queda en lista de espera
     * @throws IllegalArgumentException si el usuario o el rol son nulos
     * @throws IllegalStateException si el jugador ya pertenece al lobby o a la lista de espera
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
     * Incorpora un jugador a un equipo específico, como soporte para
     * el comando de invitación y su reversión.
     *
     * @param usuario jugador que se desea incorporar
     * @param rol rol de juego asignado al jugador
     * @param lado equipo al que se incorpora el jugador
     * @return true si el jugador es incorporado correctamente; false en caso contrario
     * @throws IllegalArgumentException si el usuario, el rol o el lado son nulos
     * @throws IllegalStateException si el jugador ya es titular en algún equipo
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
     * Da de baja a un jugador titular y promueve al primer suplente disponible
     * según el orden de ingreso FIFO (RN-10, RN-17).
     *
     * @param titularSaliente jugador titular que abandona el equipo
     * @return suplente promovido o un Optional vacío si no hay suplentes
     * @throws IllegalStateException si el jugador no es titular de ningún equipo
     */
    public Optional<Usuario> darDeBajaYPromoverSuplente(Usuario titularSaliente) {
        Equipo equipoAfectado = obtenerEquipoDe(titularSaliente)
                .orElseThrow(() -> new IllegalStateException("El usuario no es titular en ningún equipo."));

        equipoAfectado.removerJugador(titularSaliente);

        // RN-10 / RN-17: se prioriza al suplente con mayor antigüedad en la lista.
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
     * Intercambia dos jugadores titulares de equipos distintos,
     * conservando sus respectivos roles de juego.
     *
     * @param jugador1 primer jugador que se desea intercambiar
     * @param jugador2 segundo jugador que se desea intercambiar
     * @throws IllegalStateException si algún jugador no es titular
     *         o si ambos pertenecen al mismo equipo
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

    /**
     * Verifica si un usuario es titular en alguno de los equipos.
     *
     * @param usuario jugador que se desea consultar
     * @return true si pertenece a un equipo como titular; false en caso contrario
     */
    public boolean esTitular(Usuario usuario) {
        return equipoA.contieneJugador(usuario) || equipoB.contieneJugador(usuario);
    }

    /**
     * Verifica si un usuario se encuentra en la lista de espera.
     *
     * @param usuario jugador que se desea consultar
     * @return true si está en la lista de espera; false en caso contrario
     */
    public boolean estaEnListaEspera(Usuario usuario) {
        return listaEspera.containsKey(usuario);
    }

    /**
     * Obtiene el equipo al que pertenece un jugador titular.
     *
     * @param usuario jugador cuyo equipo se desea consultar
     * @return equipo del jugador o un Optional vacío si no es titular
     */
    public Optional<Equipo> obtenerEquipoDe(Usuario usuario) {
        if (equipoA.contieneJugador(usuario))
            return Optional.of(equipoA);
        if (equipoB.contieneJugador(usuario))
            return Optional.of(equipoB);
        return Optional.empty();
    }

    /**
     * Verifica si ambos equipos alcanzaron su capacidad máxima.
     *
     * @return true si ambos equipos están completos; false en caso contrario
     */
    public boolean lobbyCompleto() {
        return equipoA.estaCompleto() && equipoB.estaCompleto();
    }

    /**
     * Obtiene el equipo A del scrim.
     *
     * @return equipo A
     */
    public Equipo getEquipoA() {
        return equipoA;
    }

    /**
     * Obtiene el equipo B del scrim.
     *
     * @return equipo B
     */
    public Equipo getEquipoB() {
        return equipoB;
    }

    /**
     * Obtiene los suplentes respetando su orden de ingreso FIFO.
     *
     * @return lista ordenada de usuarios en espera
     */
    public List<Usuario> getListaEsperaOrdenada() {
        return List.copyOf(listaEspera.keySet());
    }

    /**
     * Obtiene el scrim asociado al lobby.
     *
     * @return scrim administrado
     */
    public Scrim getScrim() {
        return scrim;
    }
}

