package com.escrims.service.moderation;

import com.escrims.domain.builder.ScrimBuilder;
import com.escrims.domain.entities.Formato;
import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Rango;
import com.escrims.domain.entities.Region;
import com.escrims.domain.entities.RegistroIncidencia;
import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.TipoInfraccion;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.moderation.PoliticaSanciones;
import com.escrims.domain.observer.DomainEvent;
import com.escrims.domain.observer.DomainEventBus;
import com.escrims.domain.observer.SancionAplicadaEvent;
import com.escrims.domain.state.CanceladoState;
import com.escrims.domain.state.EnJuegoState;
import com.escrims.domain.state.LobbyArmadoState;
import com.escrims.domain.state.ScrimContext;
import com.escrims.infrastructure.persistence.memory.InMemoryRegistroIncidenciaRepository;
import com.escrims.infrastructure.persistence.memory.InMemoryScrimRepository;
import com.escrims.infrastructure.persistence.memory.InMemoryUsuarioRepository;
import com.escrims.infrastructure.scheduler.ScrimScheduler;
import com.escrims.service.AuthService;
import com.escrims.service.exception.AccesoDenegadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del registro de abandonos y no-shows y de su efecto sobre
 * strikes, cooldowns y la elegibilidad para postularse (RN-06).
 */
class PenalizacionServiceTest {

    private InMemoryScrimRepository scrims;
    private DomainEventBus eventBus;
    private PenalizacionService penalizaciones;
    private List<DomainEvent> eventos;

    private Usuario organizador;
    private Usuario jugadorA;
    private Usuario jugadorB;

    @BeforeEach
    void setUp() {
        scrims = new InMemoryScrimRepository();
        eventBus = new DomainEventBus();
        eventos = new ArrayList<>();
        eventBus.subscribe(eventos::add);
        penalizaciones = new PenalizacionService(scrims, new InMemoryRegistroIncidenciaRepository(),
                new AuthService(new InMemoryUsuarioRepository(), ""), eventBus, new PoliticaSanciones());

        organizador = jugador("organizador");
        jugadorA = jugador("jugador_a");
        jugadorB = jugador("jugador_b");
    }

    private Usuario jugador(String username) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setEmailVerificado(true);
        u.setRangoActual(Rango.PLATINO);
        return u;
    }

    /** Crea un scrim 1v1 con el lobby armado por jugadorA y jugadorB. */
    private Scrim scrimConLobbyArmado() {
        Scrim scrim = new ScrimBuilder()
                .organizador(organizador)
                .juego(Juego.VALORANT)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.LAS)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .build();
        ScrimContext ctx = new ScrimContext(scrim);
        ctx.postular(jugadorA, RolJuego.DUELISTA);
        ctx.postular(jugadorB, RolJuego.CENTINELA);
        assertInstanceOf(LobbyArmadoState.class, scrim.getEstado());
        scrims.guardar(scrim);
        return scrim;
    }

    private Scrim scrimEnJuego() {
        Scrim scrim = scrimConLobbyArmado();
        ScrimContext ctx = new ScrimContext(scrim);
        ctx.confirmar(jugadorA);
        ctx.confirmar(jugadorB);
        ctx.iniciar();
        assertInstanceOf(EnJuegoState.class, scrim.getEstado());
        return scrim;
    }

    @Test
    @DisplayName("Abandono: suma un strike, queda en el historial del jugador y publica el evento")
    void abandonoSumaStrikeYQuedaRegistrado() {
        Scrim scrim = scrimEnJuego();

        RegistroIncidencia incidencia = penalizaciones.registrarIncidencia(
                scrim.getId(), organizador, jugadorA, TipoInfraccion.ABANDONO, "Se desconectó en la ronda 3");

        assertEquals(TipoInfraccion.ABANDONO, incidencia.getTipo());
        assertEquals(1, jugadorA.getStrikes());
        assertEquals(1, jugadorA.getAbandonos());
        assertNull(incidencia.getSancion().getCooldownHasta(), "El primer strike es solo una advertencia.");
        assertEquals(1, penalizaciones.incidenciasDe(jugadorA.getId()).size());
        assertTrue(eventos.stream().anyMatch(e -> e instanceof SancionAplicadaEvent s
                && s.usuarioId().equals(jugadorA.getId()) && s.strikesTotales() == 1));
    }

    @Test
    @DisplayName("Reincidencia: el segundo strike activa un cooldown que impide postularse")
    void segundoStrikeBloqueaNuevasPostulaciones() {
        penalizaciones.registrarIncidencia(scrimEnJuego().getId(), organizador, jugadorA,
                TipoInfraccion.NO_SHOW, null);
        penalizaciones.registrarIncidencia(scrimEnJuego().getId(), organizador, jugadorA,
                TipoInfraccion.ABANDONO, null);

        assertEquals(2, jugadorA.getStrikes());
        assertEquals(1, jugadorA.getNoShows());
        assertTrue(jugadorA.tieneCooldownActivo());

        Scrim nuevo = new ScrimBuilder()
                .organizador(organizador)
                .juego(Juego.VALORANT)
                .formato(Formato.UNO_VS_UNO)
                .region(Region.LAS)
                .fechaHora(LocalDateTime.now().plusHours(3))
                .build();
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new ScrimContext(nuevo).postular(jugadorA, RolJuego.DUELISTA));
        assertTrue(ex.getMessage().contains("cooldown"));
    }

    @Test
    @DisplayName("Reglas: no admite incidencias antes del inicio, duplicadas, de ajenos ni sin permiso")
    void validaEstadoDuplicadosParticipacionYPermisos() {
        Scrim sinIniciar = scrimConLobbyArmado();
        assertThrows(IllegalStateException.class, () -> penalizaciones.registrarIncidencia(
                sinIniciar.getId(), organizador, jugadorA, TipoInfraccion.NO_SHOW, null));

        Scrim enJuego = scrimEnJuego();
        Usuario ajeno = jugador("ajeno");
        assertThrows(IllegalArgumentException.class, () -> penalizaciones.registrarIncidencia(
                enJuego.getId(), organizador, ajeno, TipoInfraccion.ABANDONO, null));
        assertThrows(AccesoDenegadoException.class, () -> penalizaciones.registrarIncidencia(
                enJuego.getId(), jugadorB, jugadorA, TipoInfraccion.ABANDONO, null));
        assertThrows(AccesoDenegadoException.class, () -> penalizaciones.registrarIncidencia(
                enJuego.getId(), jugadorA, jugadorA, TipoInfraccion.NO_SHOW, null));

        // Un jugador sí puede declarar su propio abandono.
        penalizaciones.registrarIncidencia(enJuego.getId(), jugadorA, jugadorA, TipoInfraccion.ABANDONO, null);
        assertThrows(IllegalStateException.class, () -> penalizaciones.registrarIncidencia(
                enJuego.getId(), organizador, jugadorA, TipoInfraccion.NO_SHOW, null));
        assertEquals(1, jugadorA.getStrikes());
    }

    @Test
    @DisplayName("Scheduler: al llegar la hora con el lobby sin confirmar cancela el scrim y registra el no-show")
    void schedulerRegistraNoShowDeQuienNoConfirmo() {
        Scrim scrim = scrimConLobbyArmado();
        new ScrimContext(scrim).confirmar(jugadorA);
        ScrimScheduler scheduler = new ScrimScheduler(scrims, eventBus, penalizaciones);

        scheduler.procesarTransicionesProgramadas(scrim.getFechaHora().minusMinutes(1));
        assertInstanceOf(LobbyArmadoState.class, scrim.getEstado());

        scheduler.procesarTransicionesProgramadas(scrim.getFechaHora());

        assertInstanceOf(CanceladoState.class, scrim.getEstado());
        assertEquals(0, jugadorA.getStrikes());
        assertEquals(1, jugadorB.getStrikes());
        assertEquals(1, jugadorB.getNoShows());
        assertEquals(TipoInfraccion.NO_SHOW, penalizaciones.incidenciasDe(jugadorB.getId()).get(0).getTipo());
    }
}
