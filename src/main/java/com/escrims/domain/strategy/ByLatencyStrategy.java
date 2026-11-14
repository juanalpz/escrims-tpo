package com.escrims.domain.strategy;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * «ConcreteStrategy» Empareja por cercanía/latencia.
 *
 * <p>El ping promedio del candidato debe estar dentro del umbral, que es el más
 * estricto entre el de la estrategia y el del scrim. Se prioriza el menor ping
 * y, a igual ping, a quienes juegan en la región del scrim.
 */
public class ByLatencyStrategy implements MatchmakingStrategy {

    private final int latenciaMaxima;

    /** @param latenciaMaxima ping máximo admitido, en milisegundos */
    public ByLatencyStrategy(int latenciaMaxima) {
        if (latenciaMaxima <= 0) {
            throw new IllegalArgumentException("La latencia máxima debe ser positiva");
        }
        this.latenciaMaxima = latenciaMaxima;
    }

    public int getLatenciaMaxima() {
        return latenciaMaxima;
    }

    @Override
    public boolean esElegible(Scrim scrim, Usuario candidato) {
        int ping = candidato.getLatenciaPromedio();
        return ping >= 0 && ping <= umbral(scrim);
    }

    @Override
    public Set<Usuario> seleccionarCandidatos(Scrim scrim, Set<Usuario> candidatos) {
        Set<Usuario> seleccionados = new LinkedHashSet<>();
        int cupos = scrim.cuposDisponibles();
        if (cupos <= 0) {
            return seleccionados;
        }
        candidatos.stream()
                .filter(c -> !scrim.getJugadores().contains(c))
                .filter(c -> esElegible(scrim, c))
                .sorted(Comparator.comparingInt(Usuario::getLatenciaPromedio)
                        .thenComparing(c -> !mismaRegion(scrim, c))
                        .thenComparing(Usuario::getUsername))
                .limit(cupos)
                .forEach(seleccionados::add);
        return seleccionados;
    }

    /** El umbral efectivo: el del scrim (si define uno) nunca se relaja. */
    private int umbral(Scrim scrim) {
        Integer scrimLat = scrim.getLatenciaMax();
        return (scrimLat != null && scrimLat > 0)
                ? Math.min(latenciaMaxima, scrimLat)
                : latenciaMaxima;
    }

    private boolean mismaRegion(Scrim scrim, Usuario candidato) {
        return scrim.getRegion() != null && Objects.equals(scrim.getRegion(), candidato.getRegion());
    }
}
