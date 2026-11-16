package com.escrims.domain.strategy;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Implementa una ConcreteStrategy del patrón Strategy que selecciona jugadores
 * según su latencia, priorizando el menor ping y la coincidencia de región.
 */
public class ByLatencyStrategy implements MatchmakingStrategy {

    /** Latencia máxima predeterminada para el matchmaking, en milisegundos. */
    public static final int LATENCIA_MAXIMA_DEFAULT = 100;

    private final int latenciaMaxima;

    /**
     * Inicializa la estrategia con una latencia máxima de 100 milisegundos.
     */
    public ByLatencyStrategy() {
        this(LATENCIA_MAXIMA_DEFAULT);
    }

    /**
     * Inicializa la estrategia con un límite de latencia personalizado.
     *
     * @param latenciaMaxima latencia máxima admitida en milisegundos
     * @throws IllegalArgumentException si la latencia máxima es menor o igual a cero
     */
    public ByLatencyStrategy(int latenciaMaxima) {
        if (latenciaMaxima <= 0) {
            throw new IllegalArgumentException("La latencia máxima debe ser positiva");
        }
        this.latenciaMaxima = latenciaMaxima;
    }

    /**
     * Obtiene la latencia máxima configurada para la estrategia.
     *
     * @return latencia máxima permitida en milisegundos
     */
    public int getLatenciaMaxima() {
        return latenciaMaxima;
    }

    /**
     * Verifica si la latencia del candidato cumple el límite efectivo
     * establecido por la estrategia y el scrim.
     *
     * @param scrim scrim para el que se evalúa al candidato
     * @param candidato jugador cuya latencia se desea verificar
     * @return true si la latencia es no negativa y no supera
     *         el umbral permitido; false en caso contrario
     */
    @Override
    public boolean esElegible(Scrim scrim, Usuario candidato) {
        int ping = candidato.getLatenciaPromedio();
        return ping >= 0 && ping <= umbral(scrim);
    }

    /**
     * Selecciona jugadores elegibles priorizando la menor latencia,
     * la coincidencia de región y el nombre de usuario como desempate.
     *
     * @param scrim scrim para el que se realiza la selección
     * @param candidatos conjunto de jugadores disponibles para evaluar
     * @return conjunto ordenado de jugadores seleccionados, sin superar
     *         los cupos disponibles
     */
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

    /**
     * Calcula el umbral efectivo de latencia utilizando el límite más
     * restrictivo entre la estrategia y el scrim.
     *
     * @param scrim scrim cuya configuración de latencia se consulta
     * @return umbral efectivo de latencia en milisegundos
     */
    private int umbral(Scrim scrim) {
        Integer scrimLat = scrim.getLatenciaMax();
        return (scrimLat != null && scrimLat > 0)
                ? Math.min(latenciaMaxima, scrimLat)
                : latenciaMaxima;
    }

    /**
     * Verifica si el candidato pertenece a la región configurada del scrim.
     *
     * @param scrim scrim cuya región se utiliza como referencia
     * @param candidato jugador cuya región se desea comparar
     * @return true si ambas regiones coinciden y están definidas;
     *         false en caso contrario
     */
    private boolean mismaRegion(Scrim scrim, Usuario candidato) {
        return scrim.getRegion() != null && Objects.equals(scrim.getRegion(), candidato.getRegion());
    }
}
