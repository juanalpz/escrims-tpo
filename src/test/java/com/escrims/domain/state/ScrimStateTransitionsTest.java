package com.escrims.domain.state;

import com.escrims.domain.builder.ScrimBuilder;
import com.escrims.domain.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica las transiciones válidas y las restricciones del ciclo de vida
 * de los scrims mediante el patrón State (RN-01, RN-03, RN-08, RN-09).
 */
class ScrimStateTransitionsTest {

    private Usuario organizador;
    private Usuario jugador1;
    private Usuario jugador2;
    private Scrim scrim1v1;
    private ScrimContext ctx;

    /**
     * Inicializa los usuarios de prueba, un scrim de formato 1v1
     * y su contexto de estados antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        organizador = new Usuario();
        organizador.setEmailVerificado(true);

        jugador1 = new Usuario();
        jugador1.setEmailVerificado(true);

        jugador2 = new Usuario();
        jugador2.setEmailVerificado(true);

        // Creamos un Scrim 1v1 (2 cupos totales)
        scrim1v1 = new ScrimBuilder()
                .organizador(organizador)
                .juego(Juego.VALORANT)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.LAS)
                .latenciaMax(80)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .build();

        ctx = new ScrimContext(scrim1v1);
    }

    /**
     * Verifica el recorrido completo del ciclo de vida, desde la búsqueda
     * de jugadores hasta la finalización del encuentro (RN-08, RN-09).
     */
    @Test
    @DisplayName("Flujo completo feliz: Buscando -> LobbyArmado -> Confirmado -> EnJuego -> Finalizado")
    void deberiaRecorrerTodoElCicloDeVidaExitosamente() {
        // 1. Estado inicial: BuscandoState (RN-08)
        assertInstanceOf(BuscandoState.class, ctx.getState());

        // Primera postulación (1/2 cupos): sigue en BuscandoState
        ctx.postular(jugador1, RolJuego.DUELIST);
        assertInstanceOf(BuscandoState.class, ctx.getState());

        // Segunda postulación (2/2 cupos): transiciona automáticamente a
        // LobbyArmadoState
        ctx.postular(jugador2, RolJuego.DUELIST);
        assertInstanceOf(LobbyArmadoState.class, ctx.getState());
        assertEquals(2, scrim1v1.getConfirmaciones().size(), "Deben generarse 2 confirmaciones pendientes.");

        // 2. Confirmaciones (RN-09): con 1/2 sigue en LobbyArmadoState
        ctx.confirmar(jugador1);
        assertInstanceOf(LobbyArmadoState.class, ctx.getState());

        // Con 2/2 (100%) transiciona automáticamente a ConfirmadoState
        ctx.confirmar(jugador2);
        assertInstanceOf(ConfirmadoState.class, ctx.getState());

        // 3. Inicio del encuentro (Scheduler): Confirmado -> EnJuegoState
        ctx.iniciar();
        assertInstanceOf(EnJuegoState.class, ctx.getState());

        // 4. Finalización del encuentro: EnJuego -> FinalizadoState
        ctx.finalizar();
        assertInstanceOf(FinalizadoState.class, ctx.getState());
    }

    /**
     * Verifica que el estado BuscandoState rechace acciones prematuras,
     * postulaciones duplicadas y usuarios sin correo verificado (RN-01).
     */
    @Test
    @DisplayName("BuscandoState: Rechaza duplicados, no verificados, fuera de rango y acciones prematuras")
    void deberiaValidarReglasEnBuscandoState() {
        // No permite confirmar, iniciar ni finalizar mientras busca jugadores
        assertThrows(IllegalStateException.class, () -> ctx.confirmar(jugador1));
        assertThrows(IllegalStateException.class, () -> ctx.iniciar());
        assertThrows(IllegalStateException.class, () -> ctx.finalizar());

        // RN-01: No permite postular usuario sin email verificado
        Usuario noVerificado = new Usuario();
        noVerificado.setEmailVerificado(false);
        assertThrows(IllegalStateException.class, () -> ctx.postular(noVerificado, RolJuego.DUELIST));

        // No permite que el mismo jugador se postule dos veces
        ctx.postular(jugador1, RolJuego.DUELIST);
        assertThrows(IllegalStateException.class, () -> ctx.postular(jugador1, RolJuego.DUELIST));
    }

    /**
     * Verifica que BuscandoState rechace postulaciones de jugadores
     * cuyo rango se encuentra fuera de los límites permitidos (RN-03).
     */
    @Test
    @DisplayName("RN-03: BuscandoState rechaza jugador cuyo rango está fuera de [rangoMin, rangoMax]")
    void deberiaRechazarJugadorFueraDeRango() {
        Scrim scrimConRango = new ScrimBuilder()
                .organizador(organizador)
                .juego(Juego.VALORANT)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.LAS)
                .rango(Rango.ORO, Rango.PLATINO)
                .latenciaMax(80)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .build();

        ScrimContext ctxRango = new ScrimContext(scrimConRango);

        // Bronce está fuera del rango Oro-Platino permitido en este scrim
        jugador1.asignarRango(Juego.VALORANT, Rango.BRONCE);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ctxRango.postular(jugador1, RolJuego.DUELIST));
        assertTrue(ex.getMessage().contains("fuera del límite"));
    }

    /**
     * Verifica que LobbyArmadoState impida nuevas postulaciones,
     * confirmaciones de usuarios ajenos e inicios prematuros.
     */
    @Test
    @DisplayName("LobbyArmadoState: Rechaza nuevas postulaciones, usuarios ajenos al lobby e inicio prematuro")
    void deberiaValidarReglasEnLobbyArmadoState() {
        ctx.postular(jugador1, RolJuego.DUELIST);
        ctx.postular(jugador2, RolJuego.DUELIST);
        assertInstanceOf(LobbyArmadoState.class, ctx.getState());

        Usuario intruso = new Usuario();
        intruso.setEmailVerificado(true);

        // Lobby lleno: no admite más postulaciones
        assertThrows(IllegalStateException.class, () -> ctx.postular(intruso, RolJuego.DUELIST));
        // Un usuario que no está en el lobby no puede confirmar
        assertThrows(IllegalStateException.class, () -> ctx.confirmar(intruso));
        // No se puede iniciar sin el 100% de confirmaciones
        assertThrows(IllegalStateException.class, () -> ctx.iniciar());
    }

    /**
     * Verifica que EnJuegoState impida cancelar el encuentro
     * y realizar nuevas postulaciones o confirmaciones.
     */
    @Test
    @DisplayName("EnJuegoState: Bloquea cancelaciones y nuevas postulaciones/confirmaciones")
    void deberiaBloquearCancelacionCuandoEstaEnJuego() {
        ctx.postular(jugador1, RolJuego.DUELIST);
        ctx.postular(jugador2, RolJuego.DUELIST);
        ctx.confirmar(jugador1);
        ctx.confirmar(jugador2);
        ctx.iniciar();

        assertInstanceOf(EnJuegoState.class, ctx.getState());
        assertThrows(IllegalStateException.class, () -> ctx.cancelar(), "No se puede cancelar un scrim en curso.");
        assertThrows(IllegalStateException.class, () -> ctx.postular(jugador1, RolJuego.DUELIST));
        assertThrows(IllegalStateException.class, () -> ctx.confirmar(jugador1));
    }

    /**
     * Verifica que la cancelación desde BuscandoState lleve al scrim
     * a un estado terminal que impide nuevas operaciones.
     */
    @Test
    @DisplayName("Cancelación permitida en Buscando, LobbyArmado y Confirmado; luego bloquea todo")
    void deberiaPermitirCancelarAntesDeEnJuegoYBloquearAccionesPosteriores() {
        // Caso 1: Cancelar desde BuscandoState
        ctx.cancelar();
        assertInstanceOf(CanceladoState.class, ctx.getState());

        // Un estado terminal rechaza cualquier acción posterior
        assertThrows(IllegalStateException.class, () -> ctx.postular(jugador1, RolJuego.DUELIST));
        assertThrows(IllegalStateException.class, () -> ctx.iniciar());
        assertThrows(IllegalStateException.class, () -> ctx.finalizar());
        assertThrows(IllegalStateException.class, () -> ctx.cancelar());
    }

    /**
     * Verifica que FinalizadoState sea un estado terminal
     * y rechace cualquier operación posterior sobre el scrim.
     */
    @Test
    @DisplayName("FinalizadoState: Es estado terminal y rechaza cualquier transición posterior")
    void deberiaBloquearAccionesEnFinalizadoState() {
        ctx.postular(jugador1, RolJuego.DUELIST);
        ctx.postular(jugador2, RolJuego.DUELIST);
        ctx.confirmar(jugador1);
        ctx.confirmar(jugador2);
        ctx.iniciar();
        ctx.finalizar();

        assertInstanceOf(FinalizadoState.class, ctx.getState());
        assertThrows(IllegalStateException.class, () -> ctx.postular(jugador1, RolJuego.DUELIST));
        assertThrows(IllegalStateException.class, () -> ctx.confirmar(jugador1));
        assertThrows(IllegalStateException.class, () -> ctx.iniciar());
        assertThrows(IllegalStateException.class, () -> ctx.finalizar());
        assertThrows(IllegalStateException.class, () -> ctx.cancelar());
    }
}
