package com.escrims.domain.strategy;

import com.escrims.domain.builder.ScrimBuilder;
import com.escrims.domain.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MatchmakingLoadTest {

    private static final int CANTIDAD_CANDIDATOS = 500;
    private static final long LIMITE_MS = 2000L;

    private Scrim scrimObjetivo;
    private List<Usuario> candidatos500;

    @BeforeEach
    void setUp() {
        Usuario organizador = new Usuario();
        organizador.setEmailVerificado(true);

        scrimObjetivo = new ScrimBuilder()
                .organizador(organizador)
                .juego(Juego.VALORANT)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .rango(Rango.ORO, Rango.DIAMANTE)
                .latenciaMax(80)
                .fechaHora(LocalDateTime.now().plusHours(2))
                .duracion(60)
                .build();

        candidatos500 = generarLoteCandidatos(CANTIDAD_CANDIDATOS);
    }

    private List<Usuario> generarLoteCandidatos(int cantidad) {
        List<Usuario> lista = new ArrayList<>(cantidad);
        Rango[] rangos = Rango.values();
        RolJuego[] roles = RolJuego.values();

        for (int i = 0; i < cantidad; i++) {
            Usuario u = new Usuario();
            u.setUsername("jugador_load_" + i);
            u.setEmail("load" + i + "@escrims.gg");
            u.setEmailVerificado(true);
            u.setRegion(Region.LAS);
            u.setRangoActual(rangos[i % rangos.length]);
            u.setLatenciaPromedio(20 + (i % 120)); // Entre 20ms y 139ms
            u.setRolesPreferidos(Set.of(roles[i % roles.length]));
            lista.add(u);
        }
        return lista;
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("RN-16: ByMMRStrategy procesa 500 candidatos en menos de 2 segundos")
    void deberiaProcesar500CandidatosPorMMRMenosDe2Segundos() {
        MatchmakingStrategy strategy = new ByMMRStrategy();

        long inicioNs = System.nanoTime();
        var seleccionados = strategy.seleccionar(new LinkedHashSet<>(candidatos500), scrimObjetivo);
        long duracionMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioNs);

        System.out.printf("[LOAD TEST] ByMMRStrategy -> 500 candidatos procesados en %d ms (seleccionados: %d)%n",
                duracionMs, seleccionados.size());

        assertNotNull(seleccionados);
        assertTrue(duracionMs < LIMITE_MS, "Superó el límite de 2000 ms: " + duracionMs + " ms");
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("RN-16: ByLatencyStrategy procesa 500 candidatos en menos de 2 segundos")
    void deberiaProcesar500CandidatosPorLatenciaMenosDe2Segundos() {
        MatchmakingStrategy strategy = new ByLatencyStrategy();

        long inicioNs = System.nanoTime();
        var seleccionados = strategy.seleccionar(new LinkedHashSet<>(candidatos500), scrimObjetivo);
        long duracionMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioNs);

        System.out.printf("[LOAD TEST] ByLatencyStrategy -> 500 candidatos procesados en %d ms (seleccionados: %d)%n",
                duracionMs, seleccionados.size());

        assertNotNull(seleccionados);
        assertTrue(duracionMs < LIMITE_MS, "Superó el límite de 2000 ms: " + duracionMs + " ms");
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("RN-16: ByHistoryStrategy procesa 500 candidatos en menos de 2 segundos")
    void deberiaProcesar500CandidatosPorHistorialMenosDe2Segundos() {
        MatchmakingStrategy strategy = new ByHistoryStrategy();

        long inicioNs = System.nanoTime();
        var seleccionados = strategy.seleccionar(new LinkedHashSet<>(candidatos500), scrimObjetivo);
        long duracionMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioNs);

        System.out.printf("[LOAD TEST] ByHistoryStrategy -> 500 candidatos procesados en %d ms (seleccionados: %d)%n",
                duracionMs, seleccionados.size());

        assertNotNull(seleccionados);
        assertTrue(duracionMs < LIMITE_MS, "Superó el límite de 2000 ms: " + duracionMs + " ms");
    }
}
