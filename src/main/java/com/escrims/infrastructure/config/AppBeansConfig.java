package com.escrims.infrastructure.config;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.observer.DomainEventBus;
import com.escrims.infrastructure.scheduler.ScrimSchedulerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
public class AppBeansConfig {

    @Bean
    public DomainEventBus domainEventBus() {
        return new DomainEventBus();
    }

    @Bean
    public ScrimSchedulerRepository scrimSchedulerRepository() {
        List<Scrim> activos = new CopyOnWriteArrayList<>();
        return new ScrimSchedulerRepository() {
            @Override
            public List<Scrim> obtenerScrimsActivos() {
                return List.copyOf(activos);
            }

            @Override
            public void guardar(Scrim scrim) {
                if (!activos.contains(scrim)) {
                    activos.add(scrim);
                }
            }
        };
    }
}
