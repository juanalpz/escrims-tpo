package com.escrims.domain.moderation;

import com.escrims.domain.entities.ReporteConducta;
import com.escrims.domain.entities.ResultadoModeracion;
import com.escrims.domain.entities.Sancion;
import com.escrims.domain.entities.TipoInfraccion;

import java.time.LocalDateTime;

/**
 * Segundo eslabón del patrón Chain of Responsibility: sanciona de forma
 * automática a los jugadores con menos de
 * {@link PoliticaSanciones#STRIKES_REVISION_HUMANA} strikes, aplicando un
 * strike con el cooldown escalonado de {@link PoliticaSanciones}.
 */
public class BotModeratorHandler extends ReportHandler {

    private final PoliticaSanciones politica;

    /**
     * Crea el manejador con la política de sanciones predeterminada.
     */
    public BotModeratorHandler() {
        this(new PoliticaSanciones());
    }

    /**
     * Crea el manejador con una política de sanciones específica.
     *
     * @param politica política que define strikes y cooldowns
     * @throws IllegalArgumentException si la política es nula
     */
    public BotModeratorHandler(PoliticaSanciones politica) {
        if (politica == null) {
            throw new IllegalArgumentException("La política de sanciones es obligatoria.");
        }
        this.politica = politica;
    }

    /**
     * Sanciona automáticamente al reportado si todavía no alcanzó el umbral
     * de reincidencia que exige revisión humana.
     *
     * @param reporte reporte de conducta que se desea evaluar
     * @return resultado con sanción si el caso es de reincidencia leve;
     *         resultado sin resolver en caso contrario
     * @throws IllegalStateException si el reporte ya fue resuelto o rechazado
     */
    @Override
    protected ResultadoModeracion intentarResolver(ReporteConducta reporte) {
        int strikes = reporte.getReportado().getStrikes();
        if (strikes >= PoliticaSanciones.STRIKES_REVISION_HUMANA) {
            return ResultadoModeracion.sinResolver();
        }

        Sancion sancion = politica.crearSancion(reporte.getReportado(), TipoInfraccion.CONDUCTA,
                reporte.getMotivo(), LocalDateTime.now());
        reporte.resolverConSancion(sancion);
        return ResultadoModeracion.resueltoConSancion(
                "Bot-moderador: strike automático por reincidencia leve.", sancion);
    }
}
