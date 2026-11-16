package com.escrims.infrastructure.scheduler;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.observer.DomainEventBus;
import com.escrims.domain.observer.ScrimStateChanged;
import com.escrims.domain.state.ConfirmadoState;
import com.escrims.domain.state.EnJuegoState;
import com.escrims.domain.state.ScrimContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Proceso automático en segundo plano (Cron / Scheduler) encargado de ejecutar
 * las transiciones temporales del ciclo de vida de los Scrims (CU7 y CU8).
 */
@Component
public class ScrimScheduler {

    private static final Logger log = LoggerFactory.getLogger(ScrimScheduler.class);
    private static final int DURACION_POR_DEFECTO_MINUTOS = 60;

    private final ScrimSchedulerRepository repository;
    private final DomainEventBus eventBus;

    public ScrimScheduler(ScrimSchedulerRepository repository, DomainEventBus eventBus) {
        if (repository == null || eventBus == null) {
            throw new IllegalArgumentException("El repositorio y el eventBus son obligatorios.");
        }
        this.repository = repository;
        this.eventBus = eventBus;
    }

    /**
     * Ejecución periódica automática cada 30 segundos (configurable por
     * properties).
     */
    @Scheduled(fixedRateString = "${escrims.scheduler.rate-ms:30000}")
    public void ejecutarCicloAutomatico() {
        procesarTransicionesProgramadas(LocalDateTime.now());
    }

    /**
     * Evalúa todos los scrims activos contra una marca de tiempo de referencia
     * (facilita el testing determinístico sin esperas reales).
     */
    public void procesarTransicionesProgramadas(LocalDateTime referenciaActual) {
        List<Scrim> activos = repository.obtenerScrimsActivos();

        for (Scrim scrim : activos) {
            try {
                evaluarScrim(scrim, referenciaActual);
            } catch (Exception ex) {
                // Aislamos fallos individuales para no interrumpir el lote completo
                log.error("Error al procesar transición automática para el Scrim {}: {}",
                        scrim.getId(), ex.getMessage(), ex);
            }
        }
    }

    private void evaluarScrim(Scrim scrim, LocalDateTime ahora) {
        if (scrim.getFechaHora() == null || scrim.getEstado() == null) {
            return;
        }

        ScrimContext ctx = new ScrimContext(scrim);

        // 1. Confirmado -> EnJuego al alcanzar la fecha y hora pactada (CU7)
        if (ctx.getState() instanceof ConfirmadoState && !ahora.isBefore(scrim.getFechaHora())) {
            String estadoAnterior = ctx.getState().getNombreEstado();
            ctx.iniciar();
            repository.guardar(scrim);

            String nuevoEstado = ctx.getState().getNombreEstado();
            eventBus.publish(new ScrimStateChanged(scrim.getId(), estadoAnterior, nuevoEstado));
            log.info("Scrim {} iniciado automáticamente ({} -> {}).", scrim.getId(), estadoAnterior, nuevoEstado);
            return;
        }

        // 2. EnJuego -> Finalizado al cumplirse la duración estimada de la partida
        // (CU8)
        if (ctx.getState() instanceof EnJuegoState) {
            int duracionMin = (scrim.getDuracion() != null && scrim.getDuracion() > 0) ? scrim.getDuracion()
                    : DURACION_POR_DEFECTO_MINUTOS;
            LocalDateTime horaFinEstimada = scrim.getFechaHora().plusMinutes(duracionMin);

            if (!ahora.isBefore(horaFinEstimada)) {
                String estadoAnterior = ctx.getState().getNombreEstado();
                ctx.finalizar();
                repository.guardar(scrim);

                String nuevoEstado = ctx.getState().getNombreEstado();
                eventBus.publish(new ScrimStateChanged(scrim.getId(), estadoAnterior, nuevoEstado));
                log.info("Scrim {} finalizado automáticamente por tiempo ({} -> {}).",
                        scrim.getId(), estadoAnterior, nuevoEstado);
            }
        }
    }
}
