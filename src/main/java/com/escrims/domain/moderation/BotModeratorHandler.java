package com.escrims.domain.moderation;

import com.escrims.domain.entities.ReporteConducta;
import com.escrims.domain.entities.ResultadoModeracion;
import com.escrims.domain.entities.Sancion;

import java.time.LocalDateTime;

/**
 * Implementa un ConcreteHandler del patrón Chain of Responsibility
 * que resuelve reportes de usuarios con 1 o 2 strikes previos,
 * aplicando una sanción automática por reincidencia leve.
 */
public class BotModeratorHandler extends ReportHandler {

    private static final int UMBRAL_STRIKES_BOT = 3;
    private static final int HORAS_COOLDOWN = 24;

    /**
     * Evalúa el historial de strikes del usuario reportado y aplica
     * una sanción automática cuando corresponde.
     *
     * @param reporte reporte de conducta que se desea evaluar
     * @return resultado con sanción si el usuario tiene entre 1 y 2 strikes;
     *         resultado sin resolver en caso contrario
     * @throws IllegalStateException si el reporte ya fue resuelto o rechazado
     *         y se intenta aplicar una sanción
     */
    @Override
    protected ResultadoModeracion intentarResolver(ReporteConducta reporte) {
        int strikes = reporte.getReportado().getStrikes();

        // Regla de moderación: entre 1 y 2 strikes se aplica un cooldown de 24 horas.
        if (strikes > 0 && strikes < UMBRAL_STRIKES_BOT) {
            Sancion sancion = new Sancion(1, LocalDateTime.now().plusHours(HORAS_COOLDOWN));
            reporte.resolverConSancion(sancion);
            return ResultadoModeracion.resueltoConSancion(
                    "Bot-moderador: sanción automática por reincidencia leve.", sancion);
        }
        return ResultadoModeracion.sinResolver();
    }
}
