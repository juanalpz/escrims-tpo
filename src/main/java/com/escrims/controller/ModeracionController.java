package com.escrims.controller;

import com.escrims.controller.dto.HistorialDisciplinarioResponse;
import com.escrims.controller.dto.IncidenciaRequest;
import com.escrims.controller.dto.IncidenciaResponse;
import com.escrims.controller.dto.ReporteConductaRequest;
import com.escrims.controller.dto.ReporteResponse;
import com.escrims.controller.dto.ResolucionReporteRequest;
import com.escrims.controller.dto.SancionResponse;
import com.escrims.domain.entities.EstadoReporte;
import com.escrims.domain.entities.TipoInfraccion;
import com.escrims.domain.entities.Usuario;
import com.escrims.service.AuthService;
import com.escrims.service.ScrimService;
import com.escrims.service.moderation.ModerationService;
import com.escrims.service.moderation.PenalizacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints de moderación y penalidades: reportes de conducta (CU11),
 * registro de abandonos y no-shows, y consulta de strikes y cooldowns.
 */
@RestController
@RequestMapping("/api")
public class ModeracionController {

    private final ModerationService moderacion;
    private final PenalizacionService penalizaciones;
    private final ScrimService scrims;
    private final AuthService auth;

    public ModeracionController(ModerationService moderacion, PenalizacionService penalizaciones,
                                ScrimService scrims, AuthService auth) {
        this.moderacion = moderacion;
        this.penalizaciones = penalizaciones;
        this.scrims = scrims;
        this.auth = auth;
    }

    /**
     * Reporta la conducta de un jugador. El reporte recorre la cadena
     * auto-resolver → bot → moderador humano y la respuesta indica dónde quedó.
     */
    @PostMapping("/scrims/{id}/reportes")
    @ResponseStatus(HttpStatus.CREATED)
    public ReporteResponse reportar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id,
            @Valid @RequestBody ReporteConductaRequest request) {
        Usuario reportante = auth.autenticar(authorization);
        Usuario reportado = auth.buscarUsuario(request.reportadoId());
        ModerationService.ReporteProcesado procesado =
                moderacion.reportar(scrims.obtener(id), reportante, reportado, request.motivo());
        return ReporteResponse.fromDomain(procesado.reporte(), procesado.resultado().getResolucion());
    }

    /**
     * Registra el abandono de un jugador en un scrim en juego o finalizado.
     * Lo pueden hacer el organizador, un moderador o el propio jugador.
     */
    @PostMapping("/scrims/{id}/abandonos")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidenciaResponse registrarAbandono(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id,
            @Valid @RequestBody IncidenciaRequest request) {
        return registrarIncidencia(authorization, id, request, TipoInfraccion.ABANDONO);
    }

    /**
     * Registra que un jugador no se presentó a un scrim en juego o finalizado.
     * Lo pueden hacer el organizador o un moderador.
     */
    @PostMapping("/scrims/{id}/no-shows")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidenciaResponse registrarNoShow(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id,
            @Valid @RequestBody IncidenciaRequest request) {
        return registrarIncidencia(authorization, id, request, TipoInfraccion.NO_SHOW);
    }

    /**
     * Consulta strikes, cooldown e historial de un usuario. Solo el propio usuario o un moderador.
     */
    @GetMapping("/usuarios/{id}/sanciones")
    public HistorialDisciplinarioResponse sanciones(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id) {
        Usuario solicitante = auth.autenticar(authorization);
        if (!auth.esModerador(solicitante)) {
            auth.exigirIdentidad(solicitante, id);
        }
        Usuario usuario = auth.buscarUsuario(id);
        LocalDateTime ahora = LocalDateTime.now();
        return new HistorialDisciplinarioResponse(
                usuario.getId(),
                usuario.getStrikes(),
                usuario.tieneSancionActiva(ahora),
                usuario.getCooldownHasta(ahora),
                usuario.getScrimsJugados(),
                usuario.getAbandonos(),
                usuario.getNoShows(),
                usuario.getSanciones().stream().map(SancionResponse::fromDomain).toList(),
                penalizaciones.incidenciasDe(id).stream().map(IncidenciaResponse::fromDomain).toList());
    }

    /**
     * Lista los reportes para la bandeja de moderación. Solo moderadores.
     *
     * @param estado filtro opcional; por ejemplo {@code EN_REVISION} para los escalados a revisión humana
     */
    @GetMapping("/moderacion/reportes")
    public List<ReporteResponse> listarReportes(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(required = false) EstadoReporte estado) {
        Usuario moderador = auth.autenticar(authorization);
        return moderacion.listar(moderador, estado).stream()
                .map(r -> ReporteResponse.fromDomain(r, null))
                .toList();
    }

    /**
     * Cierra un reporte en revisión con la decisión de un moderador humano.
     */
    @PostMapping("/moderacion/reportes/{reporteId}/resolucion")
    public ReporteResponse resolverReporte(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID reporteId,
            @Valid @RequestBody ResolucionReporteRequest request) {
        Usuario moderador = auth.autenticar(authorization);
        ModerationService.ReporteProcesado procesado =
                moderacion.resolverManualmente(reporteId, moderador, request.sancionar(), request.nota());
        return ReporteResponse.fromDomain(procesado.reporte(), procesado.resultado().getResolucion());
    }

    private IncidenciaResponse registrarIncidencia(String authorization, UUID scrimId,
                                                   IncidenciaRequest request, TipoInfraccion tipo) {
        Usuario solicitante = auth.autenticar(authorization);
        Usuario jugador = auth.buscarUsuario(request.usuarioId());
        return IncidenciaResponse.fromDomain(
                penalizaciones.registrarIncidencia(scrimId, solicitante, jugador, tipo, request.detalle()));
    }
}
