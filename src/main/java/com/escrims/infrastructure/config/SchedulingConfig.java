package com.escrims.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita la ejecución de tareas programadas (@Scheduled) en Spring Boot.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
