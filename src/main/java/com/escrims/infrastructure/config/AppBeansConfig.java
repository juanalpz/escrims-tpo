package com.escrims.infrastructure.config;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.observer.DomainEventBus;
import com.escrims.infrastructure.scheduler.ScrimSchedulerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Configura los beans de infraestructura de Spring, incluyendo el bus
 * de eventos de dominio y el repositorio de scrims para el scheduler.
 */
@Configuration
public class AppBeansConfig {

    /**
     * Registra el bus de eventos de dominio utilizado por el patrón Observer.
     *
     * @return instancia del bus de eventos de dominio
     */
    @Bean
    public DomainEventBus domainEventBus() {
        return new DomainEventBus();
    }

    /**
     * Registra una implementación en memoria del repositorio utilizado
     * por el scheduler para administrar los scrims activos.
     *
     * @return repositorio de scrims activos para el scheduler
     */
    @Bean
    public ScrimSchedulerRepository scrimSchedulerRepository() {
        List<Scrim> activos = new CopyOnWriteArrayList<>();
        return new ScrimSchedulerRepository() {

            /**
             * Obtiene una copia de los scrims activos registrados.
             *
             * @return lista no modificable de scrims activos
             */
            @Override
            public List<Scrim> obtenerScrimsActivos() {
                return List.copyOf(activos);
            }

            /**
             * Registra un scrim si todavía no se encuentra en el repositorio.
             *
             * @param scrim scrim que se desea registrar
             */
            @Override
            public void guardar(Scrim scrim) {
                if (!activos.contains(scrim)) {
                    activos.add(scrim);
                }
            }
        };
    }
}
