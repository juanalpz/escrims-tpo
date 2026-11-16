package com.escrims.service.moderation;

import com.escrims.domain.entities.ReporteConducta;
import com.escrims.domain.entities.ResultadoModeracion;
import com.escrims.domain.moderation.AutoResolverHandler;
import com.escrims.domain.moderation.BotModeratorHandler;
import com.escrims.domain.moderation.HumanModeratorHandler;
import com.escrims.domain.moderation.ReportHandler;

/**
 * Servicio de aplicación que coordina la resolución de reportes de conducta
 * mediante el patrón Chain of Responsibility.
 */
public class ModerationService {

    private final ReportHandler handler;

    /**
     * Inicializa la cadena de moderación predeterminada, compuesta por
     * resolución automática, moderación por bot y revisión humana.
     */
    public ModerationService() {
        AutoResolverHandler auto = new AutoResolverHandler();
        BotModeratorHandler bot = new BotModeratorHandler();
        HumanModeratorHandler human = new HumanModeratorHandler();
        auto.setSiguiente(bot);
        bot.setSiguiente(human);
        this.handler = auto;
    }

    /**
     * Inicializa el servicio con un manejador de moderación personalizado.
     *
     * @param handler manejador inicial de la cadena de moderación
     */
    public ModerationService(ReportHandler handler) {
        this.handler = handler;
    }

    /**
     * Procesa un reporte de conducta mediante la cadena de moderación
     * configurada, delegando su resolución a los manejadores correspondientes.
     *
     * @param reporte reporte de conducta que se desea evaluar
     * @return resultado de la moderación, indicando si el reporte fue
     *         resuelto y la sanción asociada, cuando corresponda
     * @throws IllegalArgumentException si el reporte es nulo
     * @throws IllegalStateException si el estado del reporte impide
     *         ejecutar la operación de moderación correspondiente
     */
    public ResultadoModeracion resolver(ReporteConducta reporte) {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte no puede ser nulo.");
        }
        return handler.manejar(reporte);
    }
}
