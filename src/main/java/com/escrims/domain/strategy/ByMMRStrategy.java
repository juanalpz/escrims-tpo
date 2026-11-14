package com.escrims.domain.strategy;

import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * «ConcreteStrategy» Empareja por rango/MMR.
 *
 * <ul>
 *   <li>El MMR del candidato debe estar dentro del rango del scrim, si tiene uno.</li>
 *   <li>Si ya hay jugadores en el lobby, el candidato no puede alejarse más de
 *       {@code tolerancia} puntos del MMR promedio del lobby.</li>
 *   <li>Si el lobby está vacío, se elige el grupo más parejo: el de mayor tamaño
 *       cuya diferencia entre el mejor y el peor no supere la {@code tolerancia}.</li>
 * </ul>
 */
public class ByMMRStrategy implements MatchmakingStrategy {

    private static final Comparator<Usuario> POR_USERNAME = Comparator.comparing(Usuario::getUsername);

    private final int tolerancia;

    /** @param tolerancia diferencia máxima de MMR admitida */
    public ByMMRStrategy(int tolerancia) {
        if (tolerancia < 0) {
            throw new IllegalArgumentException("La tolerancia no puede ser negativa");
        }
        this.tolerancia = tolerancia;
    }

    public int getTolerancia() {
        return tolerancia;
    }

    @Override
    public boolean esElegible(Scrim scrim, Usuario candidato) {
        if (!scrim.mmrDentroDeRango(candidato.getMmr())) {
            return false;
        }
        OptionalDouble promedio = mmrPromedioDelLobby(scrim);
        return promedio.isEmpty() || Math.abs(candidato.getMmr() - promedio.getAsDouble()) <= tolerancia;
    }

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
     * Ventana deslizante sobre los candidatos ordenados por MMR: busca el grupo
     * más grande (hasta {@code cupos}) con diferencia &lt;= tolerancia y, a igual
     * tamaño, el de menor diferencia.
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

    private OptionalDouble mmrPromedioDelLobby(Scrim scrim) {
        return scrim.getJugadores().stream().mapToInt(Usuario::getMmr).average();
    }
}
