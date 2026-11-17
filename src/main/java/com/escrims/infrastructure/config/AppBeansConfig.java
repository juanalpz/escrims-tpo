package com.escrims.infrastructure.config;

import com.escrims.domain.moderation.PoliticaSanciones;
import com.escrims.domain.observer.DomainEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura los beans de dominio que no llevan anotaciones de Spring:
 * el bus de eventos y la política de sanciones.
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
     * Registra la política de strikes y cooldowns del sistema de penalidades.
     *
     * @return política de sanciones compartida por la moderación y las penalizaciones
     */
    @Bean
    public PoliticaSanciones politicaSanciones() {
        return new PoliticaSanciones();
    }
}
