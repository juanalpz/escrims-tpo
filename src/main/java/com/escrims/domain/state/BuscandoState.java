package com.escrims.domain.state;

import com.escrims.domain.entities.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementa el ConcreteState del patrón State correspondiente a la búsqueda
 * de jugadores, gestionando postulaciones y la formación del lobby (RN-08).
 */
public class BuscandoState implements ScrimState {

    /**
     * Registra y acepta la postulación de un jugador elegible, verificando
     * las condiciones de acceso y formando el lobby al completar los cupos
     * (RN-01, RN-03, RN-06, RN-08).
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que solicita incorporarse
     * @param rol rol de juego solicitado
     * @throws IllegalArgumentException si el usuario o el rol son nulos,
     *         o si el rango del jugador está fuera de los límites permitidos
     * @throws IllegalStateException si el usuario no tiene el correo verificado,
     *         posee un cooldown activo, ya está postulado o deja de cumplir
     *         los requisitos de elegibilidad al aceptar la postulación
     */
    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        if (usuario == null || rol == null) {
            throw new IllegalArgumentException("El usuario y el rol son obligatorios para postularse.");
        }

        // RN-01: el usuario debe tener el correo electrónico verificado.
        if (!usuario.isEmailVerificado()) {
            throw new IllegalStateException("El jugador debe tener su email verificado para postularse.");
        }

        // RN-06: no se permite postularse con un cooldown activo.
        if (usuario.tieneCooldownActivo()) {
            throw new IllegalStateException("El jugador posee una sanción/cooldown activo.");
        }

        Scrim scrim = ctx.getScrim();

        boolean yaPostulado = scrim.getPostulaciones().stream()
                .anyMatch(p -> p.getUsuario().equals(usuario));
        if (yaPostulado) {
            throw new IllegalStateException("El jugador ya se encuentra postulado en este scrim.");
        }

        // RN-03: se verifica el rango competitivo cuando ambos límites están definidos.
        if (scrim.getRangoMin() != null && scrim.getRangoMax() != null) {
            Rango rangoJugador = usuario.getRangoParaJuego(scrim.getJuego());
            if (rangoJugador == null ||
                    rangoJugador.ordinal() < scrim.getRangoMin().ordinal() ||
                    rangoJugador.ordinal() > scrim.getRangoMax().ordinal()) {
                throw new IllegalArgumentException("El rango del jugador está fuera del límite permitido.");
            }
        }

        Postulacion postulacion = new Postulacion(usuario, scrim, rol);
        postulacion.aceptar();

        Set<Postulacion> actualizadas = new HashSet<>(scrim.getPostulaciones());
        actualizadas.add(postulacion);
        scrim.setPostulaciones(actualizadas);

        // RN-08: al completarse los cupos se generan las confirmaciones y se arma el lobby.
        if (ctx.estaCupoCompleto()) {
            Set<Confirmacion> confirmaciones = new HashSet<>();
            for (Postulacion p : scrim.getPostulaciones()) {
                if (p.getEstado() == EstadoPostulacion.ACEPTADA) {
                    confirmaciones.add(new Confirmacion(p.getUsuario(), scrim));
                }
            }
            scrim.setConfirmaciones(confirmaciones);
            ctx.setState(new LobbyArmadoState());
        }
    }

    /**
     * Impide confirmar la participación mientras el lobby está incompleto.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que intenta confirmar su participación
     * @throws IllegalStateException siempre, porque el scrim continúa buscando jugadores
     */
    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException(
                "No se puede confirmar asistencia en estado 'Buscando'. El lobby aún no está completo.");
    }

    /**
     * Impide iniciar una partida mientras se buscan jugadores.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque la partida aún no puede iniciarse
     */
    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede iniciar la partida mientras se buscan jugadores.");
    }

    /**
     * Impide finalizar una partida que todavía no fue iniciada.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque el scrim continúa buscando jugadores
     */
    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede finalizar una partida que aún está buscando jugadores.");
    }

    /**
     * Cancela el scrim durante la búsqueda de jugadores, realizando
     * la transición al estado Cancelado (RN-12).
     *
     * @param ctx contexto que administra el estado del scrim
     */
    @Override
    public void cancelar(ScrimContext ctx) {
        // RN-12: la cancelación está permitida antes de iniciar la partida.
        ctx.setState(new CanceladoState());
    }

    /**
     * Obtiene el nombre descriptivo del estado actual.
     *
     * @return nombre del estado de búsqueda de jugadores
     */
    @Override
    public String getNombreEstado() {
        return "Buscando jugadores";
    }
}
