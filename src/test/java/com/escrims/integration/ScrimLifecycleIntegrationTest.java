package com.escrims.integration;

import com.escrims.domain.builder.ScrimBuilder;
import com.escrims.domain.command.ScrimCommandInvoker;
import com.escrims.domain.command.SwapJugadoresCommand;
import com.escrims.domain.entities.*;
import com.escrims.domain.observer.DomainEvent;
import com.escrims.domain.observer.DomainEventBus;
import com.escrims.domain.observer.ScrimStateChanged;
import com.escrims.domain.state.*;
import com.escrims.domain.strategy.ByLatencyStrategy;
import com.escrims.domain.strategy.MatchmakingStrategy;
import com.escrims.infrastructure.scheduler.ScrimScheduler;
import com.escrims.infrastructure.scheduler.ScrimSchedulerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ScrimLifecycleIntegrationTest {

    private DomainEventBus eventBus;
    private List<ScrimStateChanged> transicionesRegistradas;
    private List<Scrim> repositorioScrims;
    private ScrimScheduler scheduler;

    @BeforeEach
    void setUp() {
        eventBus = new DomainEventBus();
        transicionesRegistradas = new ArrayList<>();
        repositorioScrims = new ArrayList<>();

        eventBus.subscribe((DomainEvent event) -> {
            if (event instanceof ScrimStateChanged cambio) {
                transicionesRegistradas.add(cambio);
            }
        });

        ScrimSchedulerRepository repo = new ScrimSchedulerRepository() {
            @Override
            public List<Scrim> obtenerScrimsActivos() {
                return List.copyOf(repositorioScrims);
            }

            @Override
            public void guardar(Scrim scrim) {
                if (!repositorioScrims.contains(scrim)) {
                    repositorioScrims.add(scrim);
                }
            }
        };

        scheduler = new ScrimScheduler(repo, eventBus);
    }

    private Usuario crearJugadorApto(String username, int pingMs) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setEmail(username + "@escrims.gg");
        u.setEmailVerificado(true);
        u.setRegion(Region.LAS);
        u.setRangoActual(Rango.PLATINO);
        u.setLatenciaPromedio(pingMs);
        return u;
    }

    @Test
    @DisplayName("Integración E2E Dominio: crear -> emparejar -> armar lobby -> confirmar -> iniciar (cron) -> finalizar")
    void deberiaCompletarCicloDeVidaCompletoDeUnScrim() {
        // 1. CREAR SCRIM (Builder -> Estado inicial: BuscandoState)
        Usuario organizador = crearJugadorApto("org_capitan", 25);
        LocalDateTime horaPactada = LocalDateTime.now().plusHours(2);

        Scrim scrim = new ScrimBuilder()
                .organizador(organizador)
                .juego(Juego.VALORANT)
                .formato(Formato.TRES_VS_TRES) // 6 cupos totales (3v3)
                .region(Region.LAS)
                .rango(Rango.ORO, Rango.DIAMANTE)
                .latenciaMax(80)
                .fechaHora(horaPactada)
                .duracion(45)
                .build();

        repositorioScrims.add(scrim);
        ScrimContext context = new ScrimContext(scrim);
        GestorLobbyScrim gestorLobby = new GestorLobbyScrim(scrim);
        assertInstanceOf(BuscandoState.class, scrim.getEstado());

        // 2. EMPAREJAR CANDIDATOS (Strategy: filtra jugador con 150ms > 80ms)
        List<Usuario> candidatos = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            candidatos.add(crearJugadorApto("titular_" + i, 30 + i));
        }
        Usuario jugadorConLag = crearJugadorApto("lag_player", 150);
        candidatos.add(jugadorConLag);

        MatchmakingStrategy estrategiaLatencia = new ByLatencyStrategy();
        var aptos = new ArrayList<>(estrategiaLatencia.seleccionar(new LinkedHashSet<>(candidatos), scrim));
        assertFalse(aptos.contains(jugadorConLag), "El jugador con 150ms debe ser rechazado por la estrategia (RN-04).");
        assertEquals(6, aptos.size());

        // 3. POSTULAR Y ARMAR LOBBY (BuscandoState -> LobbyArmadoState al llenar 6/6 cupos)
        RolJuego[] roles = {RolJuego.DUELIST, RolJuego.CONTROLLER, RolJuego.INICIADOR};
        for (int i = 0; i < aptos.size(); i++) {
            Usuario jugador = aptos.get(i);
            RolJuego rol = roles[i % roles.length];
            context.postular(jugador, rol);
            gestorLobby.incorporarJugador(jugador, rol);
        }

        assertTrue(gestorLobby.lobbyCompleto());
        assertInstanceOf(LobbyArmadoState.class, scrim.getEstado());
        eventBus.publish(new ScrimStateChanged(scrim.getId(), "Buscando jugadores", "Lobby armado"));

        // 4. ACCIÓN PRE-PARTIDA DEL ORGANIZADOR (Command Pattern antes de iniciar)
        ScrimCommandInvoker invoker = new ScrimCommandInvoker();
        Usuario jEquipoA = aptos.get(0);
        Usuario jEquipoB = aptos.get(3);
        invoker.ejecutar(new SwapJugadoresCommand(gestorLobby, jEquipoA, jEquipoB));
        assertTrue(gestorLobby.getEquipoB().contieneJugador(jEquipoA));

        // 5. CONFIRMACIÓN UNÁNIME (RN-09: 100% confirman -> ConfirmadoState)
        for (Usuario titular : aptos) {
            context.confirmar(titular);
        }
        assertInstanceOf(ConfirmadoState.class, scrim.getEstado());
        eventBus.publish(new ScrimStateChanged(scrim.getId(), "Lobby armado", "Confirmado"));

        // 6. INICIO AUTOMÁTICO POR SCHEDULER (CU7: ConfirmadoState -> EnJuegoState)
        scheduler.procesarTransicionesProgramadas(scrim.getFechaHora());
        assertInstanceOf(EnJuegoState.class, scrim.getEstado());

        // Verificamos RN-12: una vez EnJuego, no se permite cancelar ni ejecutar comandos
        ScrimContext contextEnJuego = new ScrimContext(scrim);
        assertThrows(IllegalStateException.class, contextEnJuego::cancelar);
        assertThrows(IllegalStateException.class, () -> invoker.deshacerUltimo());

        // 7. FINALIZACIÓN AUTOMÁTICA POR TIEMPO (CU8: EnJuegoState -> FinalizadoState)
        scheduler.procesarTransicionesProgramadas(scrim.getFechaHora().plusMinutes(45));
        assertInstanceOf(FinalizadoState.class, scrim.getEstado());

        // Verificamos que el Observer registró las 4 transiciones clave del ciclo de vida
        assertEquals(4, transicionesRegistradas.size());
        assertEquals("Lobby armado", transicionesRegistradas.get(0).nuevoEstado());
        assertEquals("Confirmado", transicionesRegistradas.get(1).nuevoEstado());
        assertEquals("En juego", transicionesRegistradas.get(2).nuevoEstado());
        assertEquals("Finalizado", transicionesRegistradas.get(3).nuevoEstado());
    }
}