package com.escrims.domain.strategy;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;

import java.util.Set;

/**
 * «Strategy» Algoritmo de emparejamiento intercambiable.
 *
 * <p>Cada implementación concreta define un criterio distinto para elegir
 * candidatos y llenar los cupos de un scrim (MMR, latencia, historial).
 */
public interface MatchmakingStrategy {

    /**
     * Elige, entre los candidatos, a quienes ocuparán los cupos libres del scrim.
     *
     * @return los seleccionados, del mejor al peor según el criterio de la
     *         estrategia; nunca más que los cupos disponibles
     */
    Set<Usuario> seleccionarCandidatos(Scrim scrim, Set<Usuario> candidatos);

    /** Indica si el candidato cumple el criterio de la estrategia para ese scrim. */
    boolean esElegible(Scrim scrim, Usuario candidato);
}
