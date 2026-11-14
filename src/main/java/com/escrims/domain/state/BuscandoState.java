package com.escrims.domain.state;

import com.escrims.domain.entities.*;
import java.util.HashSet;
import java.util.Set;

/**
 * «ConcreteState» Estado inicial de un Scrim: buscando jugadores para
 * completar el lobby (RN-08)[cite: 5].
 */
public class BuscandoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        if (usuario == null || rol == null) {
            throw new IllegalArgumentException("El usuario y el rol son obligatorios para postularse.");
        }
        // RN-01: Usuario debe tener el email verificado[cite: 5]
        if (!usuario.isEmailVerificado()) {
            throw new IllegalStateException("El jugador debe tener su email verificado para postularse.");
        }
        // RN-06: Usuario no debe tener cooldown/sanción activa[cite: 5]
        if (usuario.tieneCooldownActivo()) {
            throw new IllegalStateException("El jugador posee una sanción/cooldown activo.");
        }

        Scrim scrim = ctx.getScrim();

        // Evitar postulaciones duplicadas del mismo usuario
        boolean yaPostulado = scrim.getPostulaciones().stream()
                .anyMatch(p -> p.getUsuario().equals(usuario));
        if (yaPostulado) {
            throw new IllegalStateException("El jugador ya se encuentra postulado en este scrim.");
        }

        // RN-03: Validar rango si el scrim tiene límites configurados[cite: 5]
        if (scrim.getRangoMin() != null && scrim.getRangoMax() != null) {
            Rango rangoJugador = usuario.getRangoParaJuego(scrim.getJuego());
            if (rangoJugador == null ||
                    rangoJugador.ordinal() < scrim.getRangoMin().ordinal() ||
                    rangoJugador.ordinal() > scrim.getRangoMax().ordinal()) {
                throw new IllegalArgumentException("El rango del jugador está fuera del límite permitido.");
            }
        }

        // Registrar y aceptar la postulación
        Postulacion postulacion = new Postulacion(usuario, scrim, rol);
        postulacion.aceptar();

        Set<Postulacion> actualizadas = new HashSet<>(scrim.getPostulaciones());
        actualizadas.add(postulacion);
        scrim.setPostulaciones(actualizadas);

        // RN-08: Si se completan todos los cupos, armar lobby y preparar
        // confirmaciones[cite: 5, 7]
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

    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException(
                "No se puede confirmar asistencia en estado 'Buscando'. El lobby aún no está completo.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede iniciar la partida mientras se buscan jugadores.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede finalizar una partida que aún está buscando jugadores.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        // Cancelar permitido antes de EnJuegoState[cite: 5, 7]
        ctx.setState(new CanceladoState());
    }

    @Override
    public String getNombreEstado() {
        return "Buscando jugadores";
    }
}