package com.escrims.domain.matchmaking;

import com.escrims.domain.model.Scrim;
import com.escrims.domain.model.Usuario;

import java.util.Set;

/**
 * «Strategy» Algoritmo de emparejamiento intercambiable.
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
