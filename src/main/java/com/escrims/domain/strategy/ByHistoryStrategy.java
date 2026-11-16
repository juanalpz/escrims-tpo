package com.escrims.domain.strategy;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementa una ConcreteStrategy del patrón Strategy que selecciona jugadores
 * según su compatibilidad de roles, historial de participación y fair play.
 */
public class ByHistoryStrategy implements MatchmakingStrategy {

    /** Tasa máxima de abandono permitida para participar en el matchmaking. */
    public static final double TASA_ABANDONO_MAXIMA = 0.5;

    /** Puntaje mínimo requerido para considerar elegible a un candidato. */
    public static final double PUNTAJE_MINIMO = 0.4;

    /** Penalización aplicada al puntaje de fair play por cada strike acumulado. */
    public static final double PENALIDAD_POR_STRIKE = 0.15;

    /** Peso predeterminado de la compatibilidad de roles en el puntaje final. */
    public static final double PESO_COMPATIBILIDAD_DEFAULT = 0.5;

    private static final double SINERGIA_SIN_ROLES_DECLARADOS = 0.5;

    private final double pesoCompatibilidad;
    private final Clock reloj;

    /**
     * Inicializa la estrategia con pesos equilibrados entre compatibilidad
     * de roles y fair play, utilizando el reloj del sistema.
     */
    public ByHistoryStrategy() {
        this(PESO_COMPATIBILIDAD_DEFAULT, Clock.systemDefaultZone());
    }

    /**
     * Inicializa la estrategia con un peso de compatibilidad personalizado.
     *
     * @param pesoCompatibilidad peso de la compatibilidad de roles entre 0 y 1
     * @throws IllegalArgumentException si el peso está fuera del rango permitido
     */
    public ByHistoryStrategy(double pesoCompatibilidad) {
        this(pesoCompatibilidad, Clock.systemDefaultZone());
    }

    /**
     * Inicializa la estrategia con un peso de compatibilidad y un reloj específicos.
     *
     * @param pesoCompatibilidad peso de la compatibilidad de roles entre 0 y 1
     * @param reloj reloj utilizado para evaluar la vigencia de las sanciones
     * @throws IllegalArgumentException si el peso está fuera del rango permitido
     */
    public ByHistoryStrategy(double pesoCompatibilidad, Clock reloj) {
        if (pesoCompatibilidad < 0 || pesoCompatibilidad > 1) {
            throw new IllegalArgumentException("El peso de compatibilidad debe estar entre 0 y 1");
        }
        this.pesoCompatibilidad = pesoCompatibilidad;
        this.reloj = reloj;
    }

    /**
     * Obtiene el peso asignado a la compatibilidad de roles.
     *
     * @return peso de compatibilidad configurado
     */
    public double getPesoCompatibilidad() {
        return pesoCompatibilidad;
    }

    /**
     * Verifica la elegibilidad de un candidato según sus sanciones,
     * tasa de abandono y puntaje de compatibilidad.
     *
     * @param scrim scrim para el que se evalúa al candidato
     * @param candidato jugador cuya elegibilidad se desea verificar
     * @return true si cumple todos los criterios de selección;
     *         false en caso contrario
     */
    @Override
    public boolean esElegible(Scrim scrim, Usuario candidato) {
        if (candidato.tieneSancionActiva(LocalDateTime.now(reloj))) {
            return false;
        }
        if (candidato.getTasaAbandono() > TASA_ABANDONO_MAXIMA) {
            return false;
        }
        return calcularPuntaje(scrim, candidato) >= PUNTAJE_MINIMO;
    }

    /**
     * Selecciona jugadores elegibles priorizando la cobertura de roles
     * requeridos y completando los cupos restantes según su puntaje.
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
        List<Usuario> porPuntaje = new ArrayList<>(candidatos.stream()
                .filter(c -> !scrim.getJugadores().contains(c))
                .filter(c -> esElegible(scrim, c))
                .sorted(Comparator.<Usuario>comparingDouble(c -> calcularPuntaje(scrim, c)).reversed()
                        .thenComparing(Usuario::getUsername))
                .toList());

        // Se prioriza cubrir los roles requeridos antes de completar los cupos por puntaje.
        for (RolJuego rol : scrim.getRolesBuscados()) {
            if (seleccionados.size() == cupos) {
                break;
            }
            Optional<Usuario> mejorParaElRol = porPuntaje.stream()
                    .filter(c -> !seleccionados.contains(c))
                    .filter(c -> c.getRolesPreferidos().contains(rol))
                    .findFirst();
            mejorParaElRol.ifPresent(seleccionados::add);
        }
        for (Usuario candidato : porPuntaje) {
            if (seleccionados.size() == cupos) {
                break;
            }
            seleccionados.add(candidato);
        }
        return seleccionados;
    }

    /**
     * Calcula el puntaje ponderado de compatibilidad de un candidato,
     * combinando la sinergia de roles y el fair play.
     *
     * @param scrim scrim con el que se evalúa la compatibilidad
     * @param candidato jugador cuyo puntaje se desea calcular
     * @return puntaje ponderado de compatibilidad y fair play
     */
    public double calcularPuntaje(Scrim scrim, Usuario candidato) {
        return pesoCompatibilidad * calcularSinergia(scrim, candidato)
                + (1 - pesoCompatibilidad) * calcularFairPlay(candidato);
    }

    /**
     * Calcula la sinergia entre los roles preferidos del candidato
     * y los roles requeridos por el scrim.
     *
     * @param scrim scrim cuyos roles requeridos se desean evaluar
     * @param candidato jugador cuya compatibilidad se desea calcular
     * @return 1 si existe compatibilidad o no se exigen roles,
     *         0.5 si el candidato no declaró preferencias, o 0 en caso contrario
     */
    public double calcularSinergia(Scrim scrim, Usuario candidato) {
        Set<RolJuego> buscados = scrim.getRolesBuscados();
        if (buscados.isEmpty()) {
            return 1.0;
        }
        Set<RolJuego> preferidos = candidato.getRolesPreferidos();
        if (preferidos.isEmpty()) {
            return SINERGIA_SIN_ROLES_DECLARADOS;
        }
        return Collections.disjoint(buscados, preferidos) ? 0.0 : 1.0;
    }

    /**
     * Calcula el puntaje de fair play a partir de la reputación,
     * la tasa de abandono y las penalizaciones por strikes.
     *
     * @param candidato jugador cuyo comportamiento se desea evaluar
     * @return puntaje de fair play limitado al intervalo entre 0 y 1
     */
    public double calcularFairPlay(Usuario candidato) {
        double reputacion = candidato.getRatingFairPlay() / Usuario.RATING_MAXIMO;
        double constancia = 1 - candidato.getTasaAbandono();
        double base = (reputacion + constancia) / 2;
        double penalidad = candidato.getStrikes() * PENALIDAD_POR_STRIKE;
        return Math.max(0.0, Math.min(1.0, base - penalidad));
    }
}
