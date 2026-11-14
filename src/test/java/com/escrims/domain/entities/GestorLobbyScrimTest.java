package com.escrims.domain.entities;

import com.escrims.domain.builder.ScrimBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GestorLobbyScrimTest {

    private GestorLobbyScrim gestor;
    private Usuario j1, j2, suplente1, suplente2;

    @BeforeEach
    void setUp() {
        Usuario org = new Usuario();
        org.setEmailVerificado(true);

        j1 = new Usuario();
        j1.setEmailVerificado(true);
        j2 = new Usuario();
        j2.setEmailVerificado(true);
        suplente1 = new Usuario();
        suplente1.setEmailVerificado(true);
        suplente2 = new Usuario();
        suplente2.setEmailVerificado(true);

        Scrim scrim1v1 = new ScrimBuilder()
                .organizador(org)
                .juego(Juego.VALORANT)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.LAS)
                .latenciaMax(80)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .build();

        gestor = new GestorLobbyScrim(scrim1v1);
    }

    @Test
    @DisplayName("Distribuye titulares en Equipo A y Equipo B y envía excedentes a lista de espera FIFO")
    void deberiaAsignarTitularesYEnviarExcedentesAListaEspera() {
        assertTrue(gestor.incorporarJugador(j1, RolJuego.DUELIST));
        assertTrue(gestor.incorporarJugador(j2, RolJuego.CONTROLLER));
        assertTrue(gestor.lobbyCompleto());

        assertFalse(gestor.incorporarJugador(suplente1, RolJuego.CENTINELA));
        assertFalse(gestor.incorporarJugador(suplente2, RolJuego.INICIADOR));

        assertEquals(2, gestor.getListaEsperaOrdenada().size());
        assertSame(suplente1, gestor.getListaEsperaOrdenada().get(0));
        assertSame(suplente2, gestor.getListaEsperaOrdenada().get(1));
    }

    @Test
    @DisplayName("Al darse de baja un titular, promueve automáticamente al primer suplente FIFO (RN-17)")
    void deberiaPromoverPrimerSuplenteCuandoBajaUnTitular() {
        gestor.incorporarJugador(j1, RolJuego.DUELIST);
        gestor.incorporarJugador(j2, RolJuego.CONTROLLER);
        gestor.incorporarJugador(suplente1, RolJuego.CENTINELA);
        gestor.incorporarJugador(suplente2, RolJuego.INICIADOR);

        Optional<Usuario> promovido = gestor.darDeBajaYPromoverSuplente(j1);

        assertTrue(promovido.isPresent());
        assertSame(suplente1, promovido.get());
        assertFalse(gestor.esTitular(j1));
        assertTrue(gestor.getEquipoA().contieneJugador(suplente1));
        assertEquals(RolJuego.CENTINELA, gestor.getEquipoA().getRolDe(suplente1));
        assertEquals(1, gestor.getListaEsperaOrdenada().size());
    }

    @Test
    @DisplayName("Permite intercambiar (swap) jugadores entre Equipo A y Equipo B y reasignar roles")
    void deberiaIntercambiarJugadoresYAsignarRoles() {
        gestor.incorporarJugador(j1, RolJuego.DUELIST);
        gestor.incorporarJugador(j2, RolJuego.CONTROLLER);

        gestor.intercambiarJugadores(j1, j2);

        assertTrue(gestor.getEquipoA().contieneJugador(j2));
        assertTrue(gestor.getEquipoB().contieneJugador(j1));

        gestor.getEquipoB().asignarRol(j1, RolJuego.INICIADOR);
        assertEquals(RolJuego.INICIADOR, gestor.getEquipoB().getRolDe(j1));
    }
}
