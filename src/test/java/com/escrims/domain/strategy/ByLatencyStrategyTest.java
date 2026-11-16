package com.escrims.domain.strategy;

import com.escrims.domain.entities.Formato;
import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Region;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica los criterios de elegibilidad y selección de jugadores
 * por latencia mediante el patrón Strategy.
 */
class ByLatencyStrategyTest {

    private static final Region LAS = Region.LAS;
    private static final Region NA = Region.NA;
    private static final Juego CS2 = Juego.CS2;

    /**
     * Crea un usuario de prueba con la latencia y región indicadas.
     *
     * @param nombre nombre del jugador
     * @param ping latencia del jugador en milisegundos
     * @param region región del jugador
     * @return usuario configurado para las pruebas de matchmaking
     */
    private static Usuario jugador(String nombre, int ping, Region region) {
        return new Usuario(nombre, 1500, ping, region);
    }

    /**
     * Crea un scrim de prueba con el formato y límite de latencia indicados.
     *
     * @param formato formato de la partida
     * @param latenciaMaxima latencia máxima permitida en milisegundos
     * @return scrim configurado para evaluar candidatos
     */
    private static Scrim scrim(Formato formato, int latenciaMaxima) {
        return new Scrim(CS2, formato, LAS, null, null, latenciaMaxima);
    }

    /**
     * Verifica que la estrategia rechace jugadores cuya latencia
     * supere el máximo permitido por el scrim.
     */
    @Test
    void noAdmiteAlJugadorQueSuperaLaLatenciaMaximaDelScrim() {
        ByLatencyStrategy estrategia = new ByLatencyStrategy(150);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, 80);

        assertFalse(estrategia.esElegible(scrim, jugador("lento", 120, LAS)));
        assertTrue(estrategia.esElegible(scrim, jugador("justo", 80, LAS)));
        assertTrue(estrategia.esElegible(scrim, jugador("rapido", 20, LAS)));
    }

    /**
     * Verifica que prevalezca el umbral de latencia de la estrategia
     * cuando sea más restrictivo que el establecido por el scrim.
     */
    @Test
    void aplicaElUmbralDeLaEstrategiaSiEsMasEstrictoQueElDelScrim() {
        ByLatencyStrategy estrategia = new ByLatencyStrategy(50);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, 80);

        assertFalse(estrategia.esElegible(scrim, jugador("medio", 60, LAS)));
        assertTrue(estrategia.esElegible(scrim, jugador("rapido", 50, LAS)));
    }

    /**
     * Verifica que se utilice el límite de latencia de la estrategia
     * cuando el scrim no establece uno propio.
     */
    @Test
    void siElScrimNoDefineLatenciaUsaLaDeLaEstrategia() {
        ByLatencyStrategy estrategia = new ByLatencyStrategy(100);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, 0);

        assertTrue(estrategia.esElegible(scrim, jugador("ok", 100, LAS)));
        assertFalse(estrategia.esElegible(scrim, jugador("lento", 101, LAS)));
    }

    /**
     * Verifica que los candidatos se ordenen por menor latencia
     * y que, ante un empate, se priorice la región del scrim.
     */
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

    /**
     * Verifica que la cantidad de candidatos seleccionados
     * no supere los cupos disponibles del scrim.
     */
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

    /**
     * Verifica que la estrategia rechace límites de latencia
     * no positivos durante su construcción.
     */
    @Test
    void rechazaLatenciaMaximaNoPositiva() {
        assertThrows(IllegalArgumentException.class, () -> new ByLatencyStrategy(0));
    }
}
