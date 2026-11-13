package com.escrims.domain.matchmaking;

import com.escrims.domain.model.Formato;
import com.escrims.domain.model.Juego;
import com.escrims.domain.model.Region;
import com.escrims.domain.model.RolJuego;
import com.escrims.domain.model.Sancion;
import com.escrims.domain.model.Scrim;
import com.escrims.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ByHistoryStrategyTest {

    private static final Region LAS = new Region("LAS", "Buenos Aires");
    private static final Juego LOL = new Juego("LoL");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-12T18:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime AHORA = LocalDateTime.now(RELOJ);
    private static final double DELTA = 1e-9;

    private static Usuario jugador(String nombre, RolJuego... roles) {
        Usuario usuario = new Usuario(nombre, 1500, 40, LAS);
        for (RolJuego rol : roles) {
            usuario.agregarRolPreferido(rol);
        }
        return usuario;
    }

    private static Scrim scrim(Formato formato, RolJuego... rolesBuscados) {
        Scrim scrim = new Scrim(LOL, formato, LAS, null, 0);
        for (RolJuego rol : rolesBuscados) {
            scrim.buscarRol(rol);
        }
        return scrim;
    }

    @Test
    void sinergiaSegunLosRolesQueBuscaElScrim() {
        ByHistoryStrategy estrategia = new ByHistoryStrategy(0.5, RELOJ);
        Scrim buscaSupport = scrim(Formato.CINCO_VS_CINCO, RolJuego.SUPPORT);

        assertEquals(1.0, estrategia.calcularSinergia(buscaSupport, jugador("sup", RolJuego.SUPPORT)), DELTA);
        assertEquals(0.5, estrategia.calcularSinergia(buscaSupport, jugador("flex")), DELTA);
        assertEquals(0.0, estrategia.calcularSinergia(buscaSupport, jugador("duel", RolJuego.DUELISTA)), DELTA);
        assertEquals(1.0, estrategia.calcularSinergia(scrim(Formato.CINCO_VS_CINCO), jugador("duel2", RolJuego.DUELISTA)), DELTA);
    }

    @Test
    void fairPlayCombinaReputacionAbandonosYStrikes() {
        ByHistoryStrategy estrategia = new ByHistoryStrategy(0.5, RELOJ);

        Usuario ejemplar = jugador("ejemplar");
        assertEquals(1.0, estrategia.calcularFairPlay(ejemplar), DELTA);

        Usuario irregular = jugador("irregular");
        irregular.setRatingFairPlay(4.0);       // reputación 0.8
        irregular.registrarHistorial(10, 2);    // constancia 0.8
        irregular.agregarSancion(new Sancion(1, AHORA.minusDays(1))); // -0.15
        assertEquals(0.65, estrategia.calcularFairPlay(irregular), DELTA);

        Usuario reincidente = jugador("reincidente");
        reincidente.setRatingFairPlay(1.0);
        reincidente.agregarSancion(new Sancion(6, AHORA.minusDays(1)));
        assertEquals(0.0, estrategia.calcularFairPlay(reincidente), DELTA);
    }

    @Test
    void elPuntajePonderaSinergiaYFairPlaySegunElPeso() {
        ByHistoryStrategy estrategia = new ByHistoryStrategy(0.25, RELOJ);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, RolJuego.TANQUE);
        Usuario candidato = jugador("duel", RolJuego.DUELISTA); // sinergia 0
        candidato.setRatingFairPlay(3.0);                        // fair play (0.6 + 1) / 2 = 0.8

        assertEquals(0.25 * 0.0 + 0.75 * 0.8, estrategia.calcularPuntaje(scrim, candidato), DELTA);
    }

    @Test
    void noEsElegibleConSancionEnCooldown() {
        ByHistoryStrategy estrategia = new ByHistoryStrategy(0.5, RELOJ);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO);

        Usuario sancionado = jugador("sancionado");
        sancionado.agregarSancion(new Sancion(1, AHORA.plusHours(2)));
        Usuario rehabilitado = jugador("rehabilitado");
        rehabilitado.agregarSancion(new Sancion(1, AHORA.minusHours(2)));

        assertFalse(estrategia.esElegible(scrim, sancionado));
        assertTrue(estrategia.esElegible(scrim, rehabilitado));
    }

    @Test
    void noEsElegibleSiAbandonaMasDeLaMitadDeSusScrims() {
        ByHistoryStrategy estrategia = new ByHistoryStrategy(0.5, RELOJ);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO);

        Usuario desertor = jugador("desertor");
        desertor.registrarHistorial(10, 6);
        Usuario enElLimite = jugador("enElLimite");
        enElLimite.registrarHistorial(10, 5);

        assertFalse(estrategia.esElegible(scrim, desertor));
        assertTrue(estrategia.esElegible(scrim, enElLimite));
    }

    @Test
    void noEsElegibleSiElPuntajeNoLlegaAlMinimo() {
        ByHistoryStrategy estrategia = new ByHistoryStrategy(0.5, RELOJ);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, RolJuego.SUPPORT);

        Usuario toxico = jugador("toxico", RolJuego.DUELISTA); // sinergia 0
        toxico.setRatingFairPlay(2.0);                          // fair play 0.7 -> puntaje 0.35

        assertFalse(estrategia.esElegible(scrim, toxico));
    }

    @Test
    void seleccionaPorPuntajeDescendente() {
        ByHistoryStrategy estrategia = new ByHistoryStrategy(0.5, RELOJ);
        Scrim scrim = scrim(Formato.UNO_VS_UNO);
        Usuario ejemplar = jugador("ejemplar");
        Usuario bueno = jugador("bueno");
        bueno.setRatingFairPlay(4.0);
        Usuario regular = jugador("regular");
        regular.setRatingFairPlay(3.0);

        Set<Usuario> seleccionados = estrategia.seleccionarCandidatos(scrim, Set.of(regular, ejemplar, bueno));

        assertEquals(List.of(ejemplar, bueno), new ArrayList<>(seleccionados));
    }

    @Test
    void cubrePrimeroLosRolesBuscadosAunqueOtroTengaMejorPuntaje() {
        ByHistoryStrategy estrategia = new ByHistoryStrategy(0.5, RELOJ);
        Scrim scrim = scrim(Formato.UNO_VS_UNO, RolJuego.SUPPORT, RolJuego.TANQUE);
        Usuario support1 = jugador("support1", RolJuego.SUPPORT);
        Usuario support2 = jugador("support2", RolJuego.SUPPORT);
        Usuario tanque = jugador("tanque", RolJuego.TANQUE);
        tanque.setRatingFairPlay(3.0); // peor puntaje que ambos supports

        Set<Usuario> seleccionados = estrategia.seleccionarCandidatos(scrim, Set.of(support1, support2, tanque));

        assertEquals(Set.of(support1, tanque), seleccionados);
    }

    @Test
    void excluyeNoElegiblesYRespetaLosCupos() {
        ByHistoryStrategy estrategia = new ByHistoryStrategy(0.5, RELOJ);
        Scrim scrim = scrim(Formato.UNO_VS_UNO);
        Usuario yaAdentro = jugador("yaAdentro");
        scrim.agregarJugador(yaAdentro);
        Usuario sancionado = jugador("sancionado");
        sancionado.agregarSancion(new Sancion(1, AHORA.plusDays(1)));
        Usuario limpio = jugador("limpio");
        Usuario otro = jugador("otro");

        Set<Usuario> seleccionados = estrategia.seleccionarCandidatos(scrim, Set.of(yaAdentro, sancionado, limpio, otro));

        assertEquals(List.of(limpio), new ArrayList<>(seleccionados));
    }

    @Test
    void rechazaPesoFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new ByHistoryStrategy(1.5));
        assertThrows(IllegalArgumentException.class, () -> new ByHistoryStrategy(-0.1));
    }
}
