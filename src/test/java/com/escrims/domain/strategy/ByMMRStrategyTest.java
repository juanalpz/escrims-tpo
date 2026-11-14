package com.escrims.domain.strategy;

import com.escrims.domain.entities.Formato;
import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Rango;
import com.escrims.domain.entities.Region;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.strategy.ByMMRStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static java.time.Duration.ofSeconds;

class ByMMRStrategyTest {

    private static final Region LAS = Region.LAS;
    private static final Juego VALORANT = Juego.VALORANT;

    private static Usuario jugador(String nombre, int mmr) {
        return new Usuario(nombre, mmr, 40, LAS);
    }

    private static Scrim scrim(Formato formato, Rango min, Rango max) {
        return new Scrim(VALORANT, formato, LAS, min, max, 0);
    }

    @Test
    void rechazaAlCandidatoFueraDelRangoDelScrim() {
        ByMMRStrategy estrategia = new ByMMRStrategy(300);
        // Gold–Plat: ORO (1200..1499) a PLATINO (1500..1800) -> 1200 a 1800
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, Rango.ORO, Rango.PLATINO);

        assertFalse(estrategia.esElegible(scrim, jugador("silver", 1000)));
        assertFalse(estrategia.esElegible(scrim, jugador("diamante", 1801)));
        assertTrue(estrategia.esElegible(scrim, jugador("gold", 1200)));
        assertTrue(estrategia.esElegible(scrim, jugador("plat", 1800)));
    }

    @Test
    void sinRangoAdmiteCualquierNivel() {
        ByMMRStrategy estrategia = new ByMMRStrategy(100);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, null, null);

        assertTrue(estrategia.esElegible(scrim, jugador("hierro", 50)));
        assertTrue(estrategia.esElegible(scrim, jugador("radiante", 3000)));
    }

    @Test
    void conLobbyArmadoExigeEstarDentroDeLaToleranciaDelPromedio() {
        ByMMRStrategy estrategia = new ByMMRStrategy(100);
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, Rango.ORO, Rango.PLATINO);
        scrim.agregarJugador(jugador("a", 1400));
        scrim.agregarJugador(jugador("b", 1600)); // promedio 1500

        assertTrue(estrategia.esElegible(scrim, jugador("justo", 1600)));
        assertTrue(estrategia.esElegible(scrim, jugador("abajo", 1400)));
        assertFalse(estrategia.esElegible(scrim, jugador("lejos", 1601)));
        assertFalse(estrategia.esElegible(scrim, jugador("muyAbajo", 1250)));
    }

    @Test
    void conLobbyArmadoSeleccionaALosMasCercanosAlPromedio() {
        ByMMRStrategy estrategia = new ByMMRStrategy(200);
        Scrim scrim = scrim(Formato.TRES_VS_TRES, Rango.ORO, Rango.PLATINO);
        for (int i = 0; i < 4; i++) {
            scrim.agregarJugador(jugador("lobby" + i, 1500));
        }
        Usuario cerca = jugador("cerca", 1510);
        Usuario medio = jugador("medio", 1450);
        Usuario lejos = jugador("lejos", 1680);
        Usuario fuera = jugador("fuera", 1750);

        Set<Usuario> seleccionados = estrategia.seleccionarCandidatos(scrim, Set.of(lejos, fuera, medio, cerca));

        assertEquals(List.of(cerca, medio), new ArrayList<>(seleccionados));
    }

    @Test
    void conLobbyVacioEligeElGrupoMasParejo() {
        ByMMRStrategy estrategia = new ByMMRStrategy(100);
        Scrim scrim = scrim(Formato.UNO_VS_UNO, null, null);
        Usuario a = jugador("a", 1000);
        Usuario b = jugador("b", 1090);
        Usuario c = jugador("c", 1500);
        Usuario d = jugador("d", 1520);

        Set<Usuario> seleccionados = estrategia.seleccionarCandidatos(scrim, Set.of(a, b, c, d));

        assertEquals(Set.of(c, d), seleccionados);
    }

    @Test
    void conLobbyVacioNoJuntaJugadoresQueSuperanLaDiferenciaMaxima() {
        ByMMRStrategy estrategia = new ByMMRStrategy(100);
        Scrim scrim = scrim(Formato.TRES_VS_TRES, null, null);
        Usuario a = jugador("a", 1000);
        Usuario b = jugador("b", 1050);
        Usuario c = jugador("c", 1100);
        Usuario d = jugador("d", 1400);
        Usuario e = jugador("e", 2000);

        Set<Usuario> seleccionados = estrategia.seleccionarCandidatos(scrim, Set.of(a, b, c, d, e));

        assertEquals(Set.of(a, b, c), seleccionados);
    }

    @Test
    void noSeleccionaMasQueLosCuposDisponiblesNiAQuienesYaEstanEnElLobby() {
        ByMMRStrategy estrategia = new ByMMRStrategy(500);
        Scrim scrim = scrim(Formato.UNO_VS_UNO, null, null);
        Usuario yaAdentro = jugador("yaAdentro", 1500);
        scrim.agregarJugador(yaAdentro);

        Set<Usuario> seleccionados = estrategia.seleccionarCandidatos(scrim,
                Set.of(yaAdentro, jugador("x", 1490), jugador("y", 1600), jugador("z", 1700)));

        assertEquals(1, seleccionados.size());
        assertFalse(seleccionados.contains(yaAdentro));
    }

    @Test
    void conLobbyCompletoNoSeleccionaANadie() {
        ByMMRStrategy estrategia = new ByMMRStrategy(500);
        Scrim scrim = scrim(Formato.UNO_VS_UNO, null, null);
        scrim.agregarJugador(jugador("a", 1500));
        scrim.agregarJugador(jugador("b", 1500));

        assertTrue(estrategia.seleccionarCandidatos(scrim, Set.of(jugador("c", 1500))).isEmpty());
    }

    @Test
    void rechazaToleranciaNegativa() {
        assertThrows(IllegalArgumentException.class, () -> new ByMMRStrategy(-1));
    }

    @Test
    void empareja500CandidatosEnMenosDeDosSegundos() {
        ByMMRStrategy estrategia = new ByMMRStrategy(150);
        // Bronce (500) a Diamante (2499)
        Scrim scrim = scrim(Formato.CINCO_VS_CINCO, Rango.BRONCE, Rango.DIAMANTE);
        Random random = new Random(42);
        Set<Usuario> candidatos = new LinkedHashSet<>();
        for (int i = 0; i < 500; i++) {
            candidatos.add(jugador("jugador" + i, 500 + random.nextInt(2000)));
        }

        Set<Usuario> seleccionados = assertTimeout(ofSeconds(2),
                () -> estrategia.seleccionarCandidatos(scrim, candidatos));

        assertEquals(10, seleccionados.size());
        int minimo = seleccionados.stream().mapToInt(Usuario::getMmr).min().orElseThrow();
        int maximo = seleccionados.stream().mapToInt(Usuario::getMmr).max().orElseThrow();
        assertTrue(maximo - minimo <= 150);
        assertTrue(minimo >= 500 && maximo <= 2499);
    }
}
