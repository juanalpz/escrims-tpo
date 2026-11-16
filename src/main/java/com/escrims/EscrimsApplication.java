package com.escrims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de eScrims que configura e inicia la aplicación
 * mediante Spring Boot.
 */
@SpringBootApplication
public class EscrimsApplication {

    /**
     * Inicia la aplicación eScrims y su contexto de Spring Boot.
     *
     * @param args argumentos de línea de comandos utilizados durante el inicio
     */
    public static void main(String[] args) {
        SpringApplication.run(EscrimsApplication.class, args);
    }
}
