package com.escrims.domain.strategy;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Implementa una ConcreteStrategy del patrón Strategy que selecciona jugadores
 * según su MMR, priorizando el equilibrio competitivo y la tolerancia configurada.
 */
public class ByMMRStrategy implements MatchmakingStrategy {

    /** Diferencia máxima predeterminada de MMR admitida entre jugadores. */
    public static final int TOLERANCIA_DEFAULT = 200;

    private static final Comparator<Usuario> POR_USERNAME = Comparator.comparing(Usuario::getUsername);

    private final int tolerancia;

    /**
     * Inicializa la estrategia con una tolerancia de 200 puntos de MMR.
     */
    public ByMMRStrategy() {
        this(TOLERANCIA_DEFAULT);
    }

    /**
     * Inicializa la estrategia con una tolerancia de MMR personalizada.
     *
     * @param tolerancia diferencia máxima de MMR permitida
     * @throws IllegalArgumentException si la tolerancia es negativa
     */
    public ByMMRStrategy(int tolerancia) {
        if (tolerancia < 0) {
            throw new IllegalArgumentException("La tolerancia no puede ser negativa");
        }
        this.tolerancia = tolerancia;
    }

    /**
     * Obtiene la tolerancia de MMR configurada para la estrategia.
     *
     * @return diferencia máxima de MMR permitida
     */
    public int getTolerancia() {
        return tolerancia;
    }

    /**
     * Verifica si el candidato cumple los límites de MMR del scrim
     * y la tolerancia respecto del promedio del lobby, cuando existen jugadores.
     *
     * @param scrim scrim para el que se evalúa al candidato
     * @param candidato jugador cuya elegibilidad se desea verificar
     * @return true si cumple los límites de MMR y la tolerancia;
     *         false en caso contrario
     */
    @Override
    public boolean esElegible(Scrim scrim, Usuario candidato) {
        if (!scrim.mmrDentroDeRango(candidato.getMmr())) {
            return false;
        }
        OptionalDouble promedio = mmrPromedioDelLobby(scrim);
        return promedio.isEmpty() || Math.abs(candidato.getMmr() - promedio.getAsDouble()) <= tolerancia;
    }

    /**
     * Selecciona jugadores elegibles según su cercanía al MMR promedio del lobby
     * o conforma el grupo más equilibrado cuando todavía no hay participantes.
     *
     * @param scrim scrim para el que se realiza la selección
     * @param candidatos conjunto de jugadores disponibles para evaluar
     * @return conjunto de jugadores seleccionados sin superar los cupos disponibles
     */
    @Override
    public Set<Usuario> seleccionarCandidatos(Scrim scrim, Set<Usuario> candidatos) {
        int cupos = scrim.cuposDisponibles();
        if (cupos <= 0) {
            return new LinkedHashSet<>();
        }
        List<Usuario> elegibles = candidatos.stream()
                .filter(c -> !scrim.getJugadores().contains(c))
                .filter(c -> esElegible(scrim, c))
                .toList();

        OptionalDouble promedio = mmrPromedioDelLobby(scrim);
        if (promedio.isPresent()) {
            return masCercanosAlLobby(elegibles, promedio.getAsDouble(), cupos);
        }
        return grupoMasParejo(elegibles, cupos);
    }

    /**
     * Selecciona los candidatos con menor diferencia respecto del MMR
     * promedio del lobby, utilizando el nombre de usuario como desempate.
     *
     * @param elegibles lista de candidatos que cumplen los requisitos
     * @param promedio MMR promedio de los jugadores del lobby
     * @param cupos cantidad máxima de jugadores que pueden seleccionarse
     * @return conjunto ordenado de candidatos más cercanos al promedio
     */
    private Set<Usuario> masCercanosAlLobby(List<Usuario> elegibles, double promedio, int cupos) {
        Set<Usuario> seleccionados = new LinkedHashSet<>();
        elegibles.stream()
                .sorted(Comparator.<Usuario>comparingDouble(c -> Math.abs(c.getMmr() - promedio))
                        .thenComparing(POR_USERNAME))
                .limit(cupos)
                .forEach(seleccionados::add);
        return seleccionados;
    }

    /**
     * Selecciona el grupo más numeroso y equilibrado mediante una ventana
     * deslizante de MMR, priorizando la menor diferencia en caso de empate.
     *
     * @param elegibles lista de candidatos que cumplen los requisitos
     * @param cupos cantidad máxima de jugadores que pueden seleccionarse
     * @return conjunto de jugadores con la menor dispersión de MMR
     *         entre los grupos de mayor tamaño
     */
    private Set<Usuario> grupoMasParejo(List<Usuario> elegibles, int cupos) {
        List<Usuario> ordenados = elegibles.stream()
                .sorted(Comparator.comparingInt(Usuario::getMmr).thenComparing(POR_USERNAME))
                .toList();

        int mejorInicio = 0;
        int mejorTamanio = 0;
        int mejorDiferencia = Integer.MAX_VALUE;
        int izquierda = 0;
        for (int derecha = 0; derecha < ordenados.size(); derecha++) {
            int mmrDerecha = ordenados.get(derecha).getMmr();
            while (mmrDerecha - ordenados.get(izquierda).getMmr() > tolerancia
                    || derecha - izquierda + 1 > cupos) {
                izquierda++;
            }
            int tamanio = derecha - izquierda + 1;
            int diferencia = mmrDerecha - ordenados.get(izquierda).getMmr();
            if (tamanio > mejorTamanio || (tamanio == mejorTamanio && diferencia < mejorDiferencia)) {
                mejorInicio = izquierda;
                mejorTamanio = tamanio;
                mejorDiferencia = diferencia;
            }
        }
        return new LinkedHashSet<>(ordenados.subList(mejorInicio, mejorInicio + mejorTamanio));
    }

    /**
     * Calcula el MMR promedio de los jugadores incorporados al lobby.
     *
     * @param scrim scrim cuyos jugadores se desean evaluar
     * @return promedio de MMR o un OptionalDouble vacío si no hay jugadores
     */
    private OptionalDouble mmrPromedioDelLobby(Scrim scrim) {
        return scrim.getJugadores().stream().mapToInt(Usuario::getMmr).average();
    }
}
