package com.escrims.domain.command;

import com.escrims.domain.builder.ScrimBuilder;
import com.escrims.domain.entities.*;
import com.escrims.domain.state.EnJuegoState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ScrimCommandTest {

    private Scrim scrim;
    private GestorLobbyScrim gestor;
    private ScrimCommandInvoker invoker;
    private Usuario j1, j2, j3;

    @BeforeEach
    void setUp() {
        Usuario org = new Usuario();
        org.setEmailVerificado(true);

        j1 = new Usuario();
        j1.setEmailVerificado(true);
        j2 = new Usuario();
        j2.setEmailVerificado(true);
        j3 = new Usuario();
        j3.setEmailVerificado(true);

        scrim = new ScrimBuilder()
                .organizador(org)
                .juego(Juego.VALORANT)
                .formato(Formato.TRES_VS_TRES)
                .region(Region.LAS)
                .latenciaMax(80)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .build();

        gestor = new GestorLobbyScrim(scrim);
        invoker = new ScrimCommandInvoker();
    }

    @Test
    @DisplayName("AsignarRolCommand cambia el rol y undo() restaura el rol original")
    void deberiaAsignarYDeshacerRol() {
        gestor.incorporarAEquipo(j1, RolJuego.DUELIST, LadoEquipo.EQUIPO_A);

        ScrimCommand cmd = new AsignarRolCommand(gestor, j1, RolJuego.CENTINELA);
        invoker.ejecutar(cmd);
        assertEquals(RolJuego.CENTINELA, gestor.getEquipoA().getRolDe(j1));

        assertTrue(invoker.deshacerUltimo());
        assertEquals(RolJuego.DUELIST, gestor.getEquipoA().getRolDe(j1));
    }

    @Test
    @DisplayName("InvitarJugadorCommand agrega al equipo indicado y undo() lo remueve")
    void deberiaInvitarYDeshacerInvitacion() {
        ScrimCommand cmd = new InvitarJugadorCommand(gestor, j3, RolJuego.CONTROLLER, LadoEquipo.EQUIPO_B);
        invoker.ejecutar(cmd);
        assertTrue(gestor.getEquipoB().contieneJugador(j3));
        assertEquals(RolJuego.CONTROLLER, gestor.getEquipoB().getRolDe(j3));

        assertTrue(invoker.deshacerUltimo());
        assertFalse(gestor.getEquipoB().contieneJugador(j3));
    }

    @Test
    @DisplayName("SwapJugadoresCommand intercambia bandos entre Equipo A y B y undo() los devuelve")
    void deberiaIntercambiarYDeshacerSwap() {
        gestor.incorporarAEquipo(j1, RolJuego.DUELIST, LadoEquipo.EQUIPO_A);
        gestor.incorporarAEquipo(j2, RolJuego.INICIADOR, LadoEquipo.EQUIPO_B);

        ScrimCommand cmd = new SwapJugadoresCommand(gestor, j1, j2);
        invoker.ejecutar(cmd);

        assertTrue(gestor.getEquipoA().contieneJugador(j2));
        assertTrue(gestor.getEquipoB().contieneJugador(j1));

        assertTrue(invoker.deshacerUltimo());
        assertTrue(gestor.getEquipoA().contieneJugador(j1));
        assertTrue(gestor.getEquipoB().contieneJugador(j2));
    }

    @Test
    @DisplayName("RN-12: Bloquea ejecución o undo de comandos si el Scrim ya está EnJuego")
    void deberiaBloquearComandosCuandoEstaEnJuego() {
        gestor.incorporarAEquipo(j1, RolJuego.DUELIST, LadoEquipo.EQUIPO_A);
        scrim.setEstado(new EnJuegoState());

        ScrimCommand cmd = new AsignarRolCommand(gestor, j1, RolJuego.CENTINELA);
        assertThrows(IllegalStateException.class, () -> invoker.ejecutar(cmd));
    }
}
