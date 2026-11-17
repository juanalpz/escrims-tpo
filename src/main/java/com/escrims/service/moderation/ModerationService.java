package com.escrims.service.moderation;

import com.escrims.domain.entities.EstadoReporte;
import com.escrims.domain.entities.ReporteConducta;
import com.escrims.domain.entities.ResultadoModeracion;
import com.escrims.domain.entities.Sancion;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.TipoInfraccion;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.moderation.AutoResolverHandler;
import com.escrims.domain.moderation.BotModeratorHandler;
import com.escrims.domain.moderation.HumanModeratorHandler;
import com.escrims.domain.moderation.PoliticaSanciones;
import com.escrims.domain.moderation.ReportHandler;
import com.escrims.domain.observer.DomainEventBus;
import com.escrims.domain.observer.SancionAplicadaEvent;
import com.escrims.domain.repository.ReporteConductaRepository;
import com.escrims.service.AuthService;
import com.escrims.service.exception.AccesoDenegadoException;
import com.escrims.service.exception.NoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación que coordina la resolución de reportes de conducta
 * mediante el patrón Chain of Responsibility (CU11):
 * {@link AutoResolverHandler} → {@link BotModeratorHandler} → {@link HumanModeratorHandler}.
 */
@Service
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);

    /**
     * Reporte ya pasado por la cadena de moderación.
     *
     * @param reporte   reporte con su estado final o en revisión
     * @param resultado resolución que devolvió la cadena
     */
    public record ReporteProcesado(ReporteConducta reporte, ResultadoModeracion resultado) {
    }

    private final ReportHandler handler;
    private final ReporteConductaRepository reportes;
    private final DomainEventBus eventBus;
    private final PoliticaSanciones politica;
    private final AuthService auth;

    /**
     * Inicializa el servicio y arma la cadena de moderación predeterminada.
     */
    public ModerationService(ReporteConductaRepository reportes, DomainEventBus eventBus,
                             PoliticaSanciones politica, AuthService auth) {
        this.reportes = reportes;
        this.eventBus = eventBus;
        this.politica = politica;
        this.auth = auth;

        AutoResolverHandler auto = new AutoResolverHandler(u -> reportes.contarContraUsuario(u.getId()));
        BotModeratorHandler bot = new BotModeratorHandler(politica);
        HumanModeratorHandler human = new HumanModeratorHandler();
        auto.setSiguiente(bot);
        bot.setSiguiente(human);
        this.handler = auto;
    }

    /**
     * Procesa un reporte de conducta mediante la cadena de moderación.
     *
     * @param reporte reporte de conducta que se desea evaluar
     * @return resultado de la moderación, con la sanción asociada cuando corresponda
     * @throws IllegalArgumentException si el reporte es nulo
     * @throws IllegalStateException si el reporte ya estaba finalizado
     */
    public ResultadoModeracion resolver(ReporteConducta reporte) {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte no puede ser nulo.");
        }
        return handler.manejar(reporte);
    }

    /**
     * Registra un reporte de conducta y lo pasa por la cadena de moderación.
     *
     * @param scrim      scrim en el que ocurrió la conducta
     * @param reportante usuario autenticado que denuncia
     * @param reportado  usuario denunciado
     * @param motivo     descripción de la conducta
     * @return el reporte guardado y la resolución de la cadena
     * @throws AccesoDenegadoException  si el reportante no participó del scrim
     * @throws IllegalStateException    si el reportante ya denunció a ese jugador por el mismo scrim
     * @throws IllegalArgumentException si el reporte es inválido (por ejemplo, autodenuncia)
     */
    public synchronized ReporteProcesado reportar(Scrim scrim, Usuario reportante, Usuario reportado, String motivo) {
        if (!reportante.equals(scrim.getOrganizador()) && !scrim.esParticipante(reportante)) {
            throw new AccesoDenegadoException("Solo quienes participaron del scrim pueden reportar conductas.");
        }
        if (reportes.existeReporte(scrim.getId(), reportante.getId(), reportado.getId())) {
            throw new IllegalStateException("Ya reportaste a este jugador por este scrim.");
        }

        ReporteConducta reporte = new ReporteConducta(scrim, reportante, reportado, motivo);
        ResultadoModeracion resultado = resolver(reporte);
        reportes.guardar(reporte);

        log.info("[AUDITORIA] Reporte {} contra usuario {} en scrim {}: estado={}, resolucion='{}'",
                reporte.getId(), reportado.getId(), scrim.getId(), reporte.getEstado(), resultado.getResolucion());
        publicarSancion(reporte);
        return new ReporteProcesado(reporte, resultado);
    }

    /**
     * Lista los reportes para la bandeja de moderación.
     *
     * @param moderador usuario autenticado; debe ser moderador
     * @param estado    estado por el que filtrar, o null para traer todos
     * @throws AccesoDenegadoException si el usuario no es moderador
     */
    public List<ReporteConducta> listar(Usuario moderador, EstadoReporte estado) {
        auth.exigirModerador(moderador);
        return reportes.buscarPorEstado(estado);
    }

    /**
     * Cierra un reporte escalado a revisión humana, con o sin sanción.
     *
     * @param reporteId identificador del reporte
     * @param moderador usuario autenticado; debe ser moderador y ajeno al reporte
     * @param sancionar true para aplicar un strike al reportado; false para desestimar
     * @param nota      fundamento de la decisión
     * @return el reporte cerrado y su resolución
     * @throws NoEncontradoException   si el reporte no existe
     * @throws AccesoDenegadoException si el usuario no es moderador o es parte del reporte
     * @throws IllegalStateException   si el reporte no está en revisión
     */
    public synchronized ReporteProcesado resolverManualmente(UUID reporteId, Usuario moderador,
                                                             boolean sancionar, String nota) {
        auth.exigirModerador(moderador);
        ReporteConducta reporte = reportes.buscarPorId(reporteId)
                .orElseThrow(() -> new NoEncontradoException("Reporte no encontrado: " + reporteId));
        if (moderador.equals(reporte.getReportado()) || moderador.equals(reporte.getReportante())) {
            throw new AccesoDenegadoException("Un moderador no puede resolver un reporte del que es parte.");
        }
        if (reporte.getEstado() != EstadoReporte.EN_REVISION) {
            throw new IllegalStateException("Solo se resuelven manualmente los reportes en estado EN_REVISION.");
        }

        String fundamento = (nota == null || nota.isBlank()) ? "sin observaciones" : nota.trim();
        ResultadoModeracion resultado;
        if (sancionar) {
            Sancion sancion = politica.crearSancion(reporte.getReportado(), TipoInfraccion.CONDUCTA,
                    reporte.getMotivo(), LocalDateTime.now());
            reporte.resolverConSancion(sancion);
            resultado = ResultadoModeracion.resueltoConSancion("Moderador humano: sanción aplicada (" + fundamento + ").",
                    sancion);
        } else {
            reporte.rechazar();
            resultado = ResultadoModeracion.resueltoSinSancion("Moderador humano: reporte desestimado (" + fundamento + ").");
        }
        reportes.actualizar(reporte);

        log.info("[AUDITORIA] Moderador {} resolvió reporte {}: estado={}, resolucion='{}'",
                moderador.getId(), reporte.getId(), reporte.getEstado(), resultado.getResolucion());
        publicarSancion(reporte);
        return new ReporteProcesado(reporte, resultado);
    }

    private void publicarSancion(ReporteConducta reporte) {
        Sancion sancion = reporte.getSancion();
        if (sancion != null) {
            Usuario reportado = reporte.getReportado();
            eventBus.publish(new SancionAplicadaEvent(reportado.getId(), reporte.getScrim().getId(),
                    TipoInfraccion.CONDUCTA, reportado.getStrikes(), sancion.getCooldownHasta()));
        }
    }
}
