package com.escrims.domain.matchmaking;

import com.escrims.domain.model.Formato;
import com.escrims.domain.model.Juego;
import com.escrims.domain.model.Region;
import com.escrims.domain.model.Scrim;
import com.escrims.domain.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ByLatencyStrategyTest {

    private static final Region LAS = new Region("LAS", "Buenos Aires");
    private static final Region NA = new Region("NA", "Virginia");
    private static final Juego CS2 = new Juego("CS2");

    private static Usuario jugador(String nombre, int ping, Region region) {
        return new Usuario(nombre, 1500, ping, region);
    }

    private static Scrim scrim(Formato formato, int latenciaMaxima) {
        return new Scrim(CS2, formato, LAS, null, latenciaMaxima);
    }

    @Test
    void noAdmiteAlJugadorQueSuperaLaLatenciaMaximaDelScrim() {
        ByLatencyStrategy estrategia = new ByLatencyStrategy(150);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, 80);

        assertFalse(estrategia.esElegible(scrim, jugador("lento", 120, LAS)));
        assertTrue(estrategia.esElegible(scrim, jugador("justo", 80, LAS)));
        assertTrue(estrategia.esElegible(scrim, jugador("rapido", 20, LAS)));
    }

    @Test
    void aplicaElUmbralDeLaEstrategiaSiEsMasEstrictoQueElDelScrim() {
        ByLatencyStrategy estrategia = new ByLatencyStrategy(50);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, 80);

        assertFalse(estrategia.esElegible(scrim, jugador("medio", 60, LAS)));
        assertTrue(estrategia.esElegible(scrim, jugador("rapido", 50, LAS)));
    }

    @Test
    void siElScrimNoDefineLatenciaUsaLaDeLaEstrategia() {
        ByLatencyStrategy estrategia = new ByLatencyStrategy(100);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, 0);

        assertTrue(estrategia.esElegible(scrim, jugador("ok", 100, LAS)));
        assertFalse(estrategia.esElegible(scrim, jugador("lento", 101, LAS)));
    }

    @Test
    void seleccionaPorMenorPingYDesempataPorRegionDelScrim() {
        ByLatencyStrategy estrategia = new ByLatencyStrategy(100);
        Scrim scrim = scrim(Formato.TRES_VS_TRES, 80);
        for (int i = 0; i < 3; i++) {
            scrim.agregarJugador(jugador("lobby" + i, 30, LAS));
        }
        Usuario rapido = jugador("rapido", 15, NA);
        Usuario local = jugador("local", 40, LAS);
        Usuario visitante = jugador("avisitante", 40, NA);
        Usuario lento = jugador("lento", 120, LAS);

        Set<Usuario> seleccionados = estrategia.seleccionarCandidatos(scrim, Set.of(lento, visitante, local, rapido));

        assertEquals(List.of(rapido, local, visitante), new ArrayList<>(seleccionados));
    }

    @Test
    void noSeleccionaMasQueLosCuposDisponibles() {
        ByLatencyStrategy estrategia = new ByLatencyStrategy(100);
        Scrim scrim = scrim(Formato.UNO_VS_UNO, 0);
        Usuario a = jugador("a", 10, LAS);
        Usuario b = jugador("b", 20, LAS);
        Usuario c = jugador("c", 30, LAS);

        Set<Usuario> seleccionados = estrategia.seleccionarCandidatos(scrim, Set.of(a, b, c));

        assertEquals(List.of(a, b), new ArrayList<>(seleccionados));
    }

    @Test
    void rechazaLatenciaMaximaNoPositiva() {
        assertThrows(IllegalArgumentException.class, () -> new ByLatencyStrategy(0));
    }
}
