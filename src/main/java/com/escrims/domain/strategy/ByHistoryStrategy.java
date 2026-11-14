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
 * «ConcreteStrategy» Empareja por historial/compatibilidad.
 *
 * <p>Calcula un puntaje entre 0 y 1 por candidato:
 * {@code pesoCompatibilidad × sinergia + (1 − pesoCompatibilidad) × fairPlay}.
 *
 * <ul>
 *   <li><b>Sinergia de roles</b>: 1 si el candidato juega alguno de los roles que
 *       el scrim busca (o si el scrim no exige roles), 0.5 si no declaró roles,
 *       0 si ninguno de sus roles sirve.</li>
 *   <li><b>Fair play</b>: promedio entre la reputación (rating de feedback) y la
 *       constancia (1 − tasa de abandono), menos una penalidad por cada strike.</li>
 * </ul>
 *
 * <p>Quedan afuera los candidatos con una sanción en cooldown, con una tasa de
 * abandono mayor a {@link #TASA_ABANDONO_MAXIMA} o con puntaje menor a
 * {@link #PUNTAJE_MINIMO}.
 */
public class ByHistoryStrategy implements MatchmakingStrategy {

    public static final double TASA_ABANDONO_MAXIMA = 0.5;
    public static final double PUNTAJE_MINIMO = 0.4;
    public static final double PENALIDAD_POR_STRIKE = 0.15;
    private static final double SINERGIA_SIN_ROLES_DECLARADOS = 0.5;

    private final double pesoCompatibilidad;
    private final Clock reloj;

    /** @param pesoCompatibilidad peso de la sinergia de roles, entre 0 y 1 */
    public ByHistoryStrategy(double pesoCompatibilidad) {
        this(pesoCompatibilidad, Clock.systemDefaultZone());
    }

    public ByHistoryStrategy(double pesoCompatibilidad, Clock reloj) {
        if (pesoCompatibilidad < 0 || pesoCompatibilidad > 1) {
            throw new IllegalArgumentException("El peso de compatibilidad debe estar entre 0 y 1");
        }
        this.pesoCompatibilidad = pesoCompatibilidad;
        this.reloj = reloj;
    }

    public double getPesoCompatibilidad() {
        return pesoCompatibilidad;
    }

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
     * Primero cubre cada rol buscado con el mejor candidato que lo juega y
     * después completa los cupos restantes por puntaje.
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

    /** Puntaje de compatibilidad del candidato con el scrim, entre 0 y 1. */
    public double calcularPuntaje(Scrim scrim, Usuario candidato) {
        return pesoCompatibilidad * calcularSinergia(scrim, candidato)
                + (1 - pesoCompatibilidad) * calcularFairPlay(candidato);
    }

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

    public double calcularFairPlay(Usuario candidato) {
        double reputacion = candidato.getRatingFairPlay() / Usuario.RATING_MAXIMO;
        double constancia = 1 - candidato.getTasaAbandono();
        double base = (reputacion + constancia) / 2;
        double penalidad = candidato.getStrikes() * PENALIDAD_POR_STRIKE;
        return Math.max(0.0, Math.min(1.0, base - penalidad));
    }
}
