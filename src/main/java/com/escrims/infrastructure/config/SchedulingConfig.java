package com.escrims.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configura y habilita la ejecución de tareas programadas mediante
 * la anotación {@link EnableScheduling} de Spring Boot.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
