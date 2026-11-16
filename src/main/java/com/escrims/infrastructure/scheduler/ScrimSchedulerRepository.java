package com.escrims.infrastructure.scheduler;

import com.escrims.domain.entities.Scrim;
import java.util.List;

/**
 * Contrato de acceso a los Scrims activos para evaluación periódica del
 * Scheduler.
 */
public interface ScrimSchedulerRepository {
    List<Scrim> obtenerScrimsActivos();

    void guardar(Scrim scrim);
}
