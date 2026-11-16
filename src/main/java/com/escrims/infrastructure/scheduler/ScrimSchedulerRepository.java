package com.escrims.infrastructure.scheduler;

import com.escrims.domain.entities.Scrim;
import java.util.List;

/**
 * Define el contrato Repository para consultar y persistir los scrims
 * activos utilizados por el scheduler de transiciones automáticas (CU7, CU8).
 */
public interface ScrimSchedulerRepository {

    /**
     * Obtiene los scrims activos que deben ser evaluados
     * por el proceso de transiciones automáticas.
     *
     * @return lista de scrims activos disponibles para su evaluación
     */
    List<Scrim> obtenerScrimsActivos();

    /**
     * Guarda un scrim después de procesar sus transiciones de estado.
     *
     * @param scrim scrim que se desea persistir
     */
    void guardar(Scrim scrim);
}
