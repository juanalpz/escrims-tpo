package com.escrims.infrastructure.scheduler;

import com.escrims.domain.builder.ScrimBuilder;
import com.escrims.domain.entities.*;
import com.escrims.domain.observer.DomainEvent;
import com.escrims.domain.observer.DomainEventBus;
import com.escrims.domain.observer.ScrimStateChanged;
import com.escrims.domain.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica las transiciones automáticas del ciclo de vida de los scrims
 * y la publicación de eventos mediante el scheduler (CU7, CU8).
 */
class ScrimSchedulerTest {

    private List<Scrim> baseEnMemoria;
    private List<DomainEvent> eventosPublicados;
    private ScrimScheduler scheduler;
    private Usuario organizador;

    /**
     * Inicializa el repositorio simulado, el bus de eventos,
     * el scheduler y el organizador antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        baseEnMemoria = new ArrayList<>();
        eventosPublicados = new ArrayList<>();

        ScrimSchedulerRepository repoFake = new ScrimSchedulerRepository() {
            /**
             * Obtiene los scrims activos almacenados en memoria.
             *
             * @return lista de scrims disponibles para su evaluación
             */
            @Override
            public List<Scrim> obtenerScrimsActivos() {
                return List.copyOf(baseEnMemoria);
            }

            /**
             * Simula la persistencia de un scrim sin realizar operaciones
             * adicionales, dado que las entidades permanecen en memoria.
             *
             * @param scrim scrim cuyo guardado se simula
             */
            @Override
            public void guardar(Scrim scrim) {
                // Persistencia simulada en memoria
            }
        };

        DomainEventBus eventBus = new DomainEventBus();
        eventBus.subscribe(eventosPublicados::add);

        scheduler = new ScrimScheduler(repoFake, eventBus);

        organizador = new Usuario();
        organizador.setEmailVerificado(true);
    }

    /**
     * Construye un scrim de prueba con la fecha, duración y estado indicados.
     *
     * @param fechaFutura fecha y hora programadas para el encuentro
     * @param duracionMinutos duración estimada de la partida en minutos
     * @param estado estado inicial que se asignará al scrim
     * @return scrim configurado para evaluar transiciones automáticas
     */
    private Scrim crearScrimBase(LocalDateTime fechaFutura, int duracionMinutos, ScrimState estado) {
        Scrim scrim = new ScrimBuilder()
                .organizador(organizador)
                .juego(Juego.VALORANT)
                .formato(Formato.CINCO_VS_CINCO)
                .region(Region.LAS)
                .latenciaMax(80)
                .fechaHora(fechaFutura)
                .duracion(duracionMinutos)
                .build();
        scrim.setEstado(estado);
        return scrim;
    }

    /**
     * Verifica el inicio automático de un scrim confirmado al alcanzar
     * la hora programada y la publicación del evento correspondiente (CU7).
     */
    @Test
    @DisplayName("CU7: Pasa automáticamente de Confirmado a EnJuego cuando llega la fecha/hora y publica evento")
    void deberiaIniciarScrimConfirmadoAlLlegarLaHora() {
        LocalDateTime horaPactada = LocalDateTime.now().plusHours(2);
        Scrim scrim = crearScrimBase(horaPactada, 60, new ConfirmadoState());
        baseEnMemoria.add(scrim);

        // Simulamos que el reloj del sistema alcanzó exactamente la hora pactada
        scheduler.procesarTransicionesProgramadas(scrim.getFechaHora());

        assertInstanceOf(EnJuegoState.class, scrim.getEstado());
        assertEquals(1, eventosPublicados.size());
        ScrimStateChanged evento = (ScrimStateChanged) eventosPublicados.get(0);
        assertEquals(scrim.getId(), evento.scrimId());
        assertEquals("Confirmado", evento.estadoAnterior());
        assertEquals("En juego", evento.nuevoEstado());
    }

    /**
     * Verifica que un scrim confirmado permanezca en su estado
     * mientras no se alcance la fecha y hora programadas.
     */
    @Test
    @DisplayName("No inicia un Scrim Confirmado si todavía falta tiempo para la fecha/hora pactada")
    void noDeberiaIniciarScrimConfirmadoAntesDeLaHora() {
        LocalDateTime horaPactada = LocalDateTime.now().plusHours(2);
        Scrim scrim = crearScrimBase(horaPactada, 60, new ConfirmadoState());
        baseEnMemoria.add(scrim);

        // Simulamos que falta 1 minuto para el inicio
        scheduler.procesarTransicionesProgramadas(scrim.getFechaHora().minusMinutes(1));

        assertInstanceOf(ConfirmadoState.class, scrim.getEstado());
        assertTrue(eventosPublicados.isEmpty());
    }

    /**
     * Verifica la finalización automática de un scrim en juego
     * al cumplirse su duración estimada y la publicación del evento (CU8).
     */
    @Test
    @DisplayName("CU8: Pasa automáticamente de EnJuego a Finalizado al cumplirse la duración estimada")
    void deberiaFinalizarScrimEnJuegoAlCumplirseDuracion() {
        LocalDateTime horaInicio = LocalDateTime.now().plusHours(2);
        Scrim scrim = crearScrimBase(horaInicio, 45, new EnJuegoState());
        baseEnMemoria.add(scrim);

        // Simulamos 30 minutos después del inicio (van 30 de 45 min) -> sigue EnJuego
        scheduler.procesarTransicionesProgramadas(scrim.getFechaHora().plusMinutes(30));
        assertInstanceOf(EnJuegoState.class, scrim.getEstado());

        // Simulamos 45 minutos después del inicio -> pasa a FinalizadoState
        scheduler.procesarTransicionesProgramadas(scrim.getFechaHora().plusMinutes(45));
        assertInstanceOf(FinalizadoState.class, scrim.getEstado());
        assertEquals(1, eventosPublicados.size());
        ScrimStateChanged evento = (ScrimStateChanged) eventosPublicados.get(0);
        assertEquals("En juego", evento.estadoAnterior());
        assertEquals("Finalizado", evento.nuevoEstado());
    }

    /**
     * Verifica que el scheduler ignore los scrims en estados
     * BuscandoState y CanceladoState, incluso después de la hora programada.
     */
    @Test
    @DisplayName("Ignora Scrims que están en BuscandoState o CanceladoState aunque haya pasado la hora")
    void deberiaIgnorarScrimsNoConfirmadosONoIniciados() {
        LocalDateTime horaPactada = LocalDateTime.now().plusHours(1);
        Scrim buscando = crearScrimBase(horaPactada, 60, new BuscandoState());
        Scrim cancelado = crearScrimBase(horaPactada, 60, new CanceladoState());
        baseEnMemoria.add(buscando);
        baseEnMemoria.add(cancelado);

        // Simulamos 2 horas después de la hora pactada
        scheduler.procesarTransicionesProgramadas(horaPactada.plusHours(2));

        assertInstanceOf(BuscandoState.class, buscando.getEstado());
        assertInstanceOf(CanceladoState.class, cancelado.getEstado());
        assertTrue(eventosPublicados.isEmpty());
    }
}
