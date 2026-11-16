package com.escrims.domain.strategy;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;

import java.util.Set;

/**
 * Define el contrato Strategy del patrón Strategy para implementar algoritmos
 * de matchmaking intercambiables según MMR, latencia o historial de jugadores.
 */
public interface MatchmakingStrategy {

    /**
     * Selecciona los candidatos que ocuparán los cupos disponibles del scrim
     * según los criterios de la estrategia implementada.
     *
     * @param scrim scrim para el que se realiza el emparejamiento
     * @param candidatos conjunto de jugadores disponibles para evaluar
     * @return conjunto de jugadores seleccionados según la prioridad
     *         de la estrategia, sin superar los cupos disponibles
     */
    Set<Usuario> seleccionarCandidatos(Scrim scrim, Set<Usuario> candidatos);

    /**
     * Delega la selección de candidatos utilizando un orden alternativo
     * de parámetros.
     *
     * @param candidatos conjunto de jugadores disponibles para evaluar
     * @param scrim scrim para el que se realiza el emparejamiento
     * @return conjunto de jugadores seleccionados según la estrategia
     * @see #seleccionarCandidatos(Scrim, Set)
     */
    default Set<Usuario> seleccionar(Set<Usuario> candidatos, Scrim scrim) {
        return seleccionarCandidatos(scrim, candidatos);
    }

    /**
     * Verifica si un candidato cumple los requisitos de emparejamiento
     * definidos por la estrategia concreta.
     *
     * @param scrim scrim para el que se evalúa al candidato
     * @param candidato jugador cuya elegibilidad se desea verificar
     * @return true si el candidato cumple los criterios de la estrategia;
     *         false en caso contrario
     */
    boolean esElegible(Scrim scrim, Usuario candidato);
}
