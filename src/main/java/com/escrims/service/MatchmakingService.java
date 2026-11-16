package com.escrims.service;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.strategy.MatchmakingStrategy;

import java.util.Set;

/**
 * Implementa el Context del patrón Strategy para gestionar y conmutar
 * dinámicamente los algoritmos de matchmaking utilizados por eScrims.
 */
public class MatchmakingService {

    private MatchmakingStrategy estrategia;

    /**
     * Inicializa el servicio con una estrategia de matchmaking.
     *
     * @param estrategia estrategia de emparejamiento que se desea utilizar
     * @throws IllegalArgumentException si la estrategia es nula
     */
    public MatchmakingService(MatchmakingStrategy estrategia) {
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia de matchmaking es obligatoria.");
        }
        this.estrategia = estrategia;
    }

    /**
     * Delega la selección de jugadores a la estrategia de matchmaking activa.
     *
     * @param scrim scrim para el que se realiza el emparejamiento
     * @param candidatos conjunto de jugadores disponibles para evaluar
     * @return conjunto de jugadores seleccionados según los criterios
     *         de la estrategia configurada
     */
    public Set<Usuario> seleccionar(Scrim scrim, Set<Usuario> candidatos) {
        return estrategia.seleccionarCandidatos(scrim, candidatos);
    }

    /**
     * Cambia la estrategia de matchmaking utilizada por el servicio
     * durante su ejecución.
     *
     * @param nuevaEstrategia nueva implementación de {@link MatchmakingStrategy}
     * @throws IllegalArgumentException si la nueva estrategia es nula
     */
    public void cambiarEstrategia(MatchmakingStrategy nuevaEstrategia) {
        if (nuevaEstrategia == null) {
            throw new IllegalArgumentException("La nueva estrategia no puede ser nula.");
        }
        this.estrategia = nuevaEstrategia;
    }

    /**
     * Obtiene la estrategia de matchmaking actualmente configurada.
     *
     * @return estrategia de emparejamiento activa
     */
    public MatchmakingStrategy getEstrategia() { return estrategia; }
}
