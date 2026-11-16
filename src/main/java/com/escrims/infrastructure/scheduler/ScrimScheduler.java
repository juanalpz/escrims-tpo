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
 * Ejecuta las transiciones temporales automáticas del ciclo de vida
 * de los scrims mediante tareas programadas de Spring (CU7, CU8).
 */
@Component
public class ScrimScheduler {

    private static final Logger log = LoggerFactory.getLogger(ScrimScheduler.class);
    private static final int DURACION_POR_DEFECTO_MINUTOS = 60;

    private final ScrimSchedulerRepository repository;
    private final DomainEventBus eventBus;

    /**
     * Inicializa el scheduler con el repositorio de scrims activos
     * y el bus de eventos de dominio.
     *
     * @param repository repositorio utilizado para consultar y guardar scrims
     * @param eventBus bus encargado de publicar los cambios de estado
     * @throws IllegalArgumentException si el repositorio o el bus de eventos son nulos
     */
    public ScrimScheduler(ScrimSchedulerRepository repository, DomainEventBus eventBus) {
        if (repository == null || eventBus == null) {
            throw new IllegalArgumentException("El repositorio y el eventBus son obligatorios.");
        }
        this.repository = repository;
        this.eventBus = eventBus;
    }

    /**
     * Ejecuta periódicamente la evaluación de transiciones automáticas
     * utilizando la fecha y hora actuales, con un intervalo predeterminado
     * de 30 segundos configurable mediante propiedades de Spring.
     */
    @Scheduled(fixedRateString = "${escrims.scheduler.rate-ms:30000}")
    public void ejecutarCicloAutomatico() {
        procesarTransicionesProgramadas(LocalDateTime.now());
    }

    /**
     * Evalúa los scrims activos y procesa las transiciones programadas
     * según una fecha y hora de referencia (CU7, CU8).
     *
     * @param referenciaActual fecha y hora utilizada para evaluar las transiciones
     */
    public void procesarTransicionesProgramadas(LocalDateTime referenciaActual) {
        List<Scrim> activos = repository.obtenerScrimsActivos();

        for (Scrim scrim : activos) {
            try {
                evaluarScrim(scrim, referenciaActual);
            } catch (Exception ex) {
                log.error("Error al procesar transición automática para el Scrim {}: {}",
                        scrim.getId(), ex.getMessage(), ex);
            }
        }
    }

    /**
     * Evalúa las condiciones temporales de un scrim y ejecuta las transiciones
     * correspondientes, persistiendo los cambios y publicando sus eventos.
     *
     * @param scrim scrim cuyo estado se desea evaluar
     * @param ahora fecha y hora de referencia para las transiciones
     */
    private void evaluarScrim(Scrim scrim, LocalDateTime ahora) {
        if (scrim.getFechaHora() == null || scrim.getEstado() == null) {
            return;
        }

        ScrimContext ctx = new ScrimContext(scrim);

        // CU7: iniciar automáticamente el scrim confirmado al alcanzar la fecha programada.
        if (ctx.getState() instanceof ConfirmadoState && !ahora.isBefore(scrim.getFechaHora())) {
            String estadoAnterior = ctx.getState().getNombreEstado();
            ctx.iniciar();
            repository.guardar(scrim);

            String nuevoEstado = ctx.getState().getNombreEstado();
            eventBus.publish(new ScrimStateChanged(scrim.getId(), estadoAnterior, nuevoEstado));
            log.info("Scrim {} iniciado automáticamente ({} -> {}).", scrim.getId(), estadoAnterior, nuevoEstado);
            return;
        }

        // CU8: finalizar automáticamente la partida al cumplirse su duración estimada.
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
