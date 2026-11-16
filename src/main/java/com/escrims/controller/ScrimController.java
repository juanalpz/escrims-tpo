package com.escrims.controller;

import com.escrims.controller.dto.AccionScrimRequest;
import com.escrims.controller.dto.AccionScrimResponse;
import com.escrims.controller.dto.CancelarScrimRequest;
import com.escrims.controller.dto.ConfirmacionRequest;
import com.escrims.controller.dto.ConfirmacionResponse;
import com.escrims.controller.dto.CrearScrimRequest;
import com.escrims.controller.dto.EstadisticaRequest;
import com.escrims.controller.dto.EstadisticaResponse;
import com.escrims.controller.dto.FinalizarScrimRequest;
import com.escrims.controller.dto.PostulacionRequest;
import com.escrims.controller.dto.PostulacionResponse;
import com.escrims.controller.dto.ScrimResponse;
import com.escrims.domain.entities.DatosScrim;
import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Rango;
import com.escrims.domain.entities.Region;
import com.escrims.domain.entities.ResultadoPartida;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;
import com.escrims.service.AuthService;
import com.escrims.service.ScrimService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints del ciclo de vida de los scrims: búsqueda, creación, postulación,
 * confirmación, acciones del organizador, cancelación, finalización y estadísticas.
 *
 * <p>Las operaciones de escritura exigen {@code Authorization: Bearer <token>} y el
 * identificador de usuario del cuerpo debe coincidir con el dueño del token.</p>
 */
@RestController
@RequestMapping("/api/scrims")
public class ScrimController {

    private final ScrimService scrims;
    private final AuthService auth;

    public ScrimController(ScrimService scrims, AuthService auth) {
        this.scrims = scrims;
        this.auth = auth;
    }

    /**
     * Busca scrims que siguen buscando jugadores. Todos los filtros son opcionales.
     *
     * @param fecha día del encuentro en formato ISO ({@code yyyy-MM-dd})
     */
    @GetMapping
    public List<ScrimResponse> buscar(
            @RequestParam(required = false) Juego juego,
            @RequestParam(required = false) Region region,
            @RequestParam(required = false) Rango rangoMin,
            @RequestParam(required = false) Rango rangoMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer latenciaMax) {
        DatosScrim filtro = new DatosScrim(juego, null, rangoMin, rangoMax, region,
                fecha != null ? fecha.atStartOfDay() : null, latenciaMax != null ? latenciaMax : 0);
        return scrims.buscar(filtro).stream().map(ScrimResponse::fromDomain).toList();
    }

    /**
     * Obtiene el resumen de un scrim en cualquier estado.
     */
    @GetMapping("/{id}")
    public ScrimResponse obtener(@PathVariable UUID id) {
        return ScrimResponse.fromDomain(scrims.obtener(id));
    }

    /**
     * Crea un scrim a nombre del usuario autenticado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScrimResponse crear(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CrearScrimRequest request) {
        Usuario organizador = autenticarComo(authorization, request.organizadorId());
        DatosScrim datos = new DatosScrim(request.juego(), request.formato(), request.rangoMin(), request.rangoMax(),
                request.region(), request.fechaHora(), request.latenciaMax());
        Scrim scrim = scrims.crear(organizador, datos, request.duracionMinutos(), request.modalidad(),
                request.reglasRoles());
        return ScrimResponse.fromDomain(scrim);
    }

    /**
     * Postula al usuario autenticado en el scrim.
     */
    @PostMapping("/{id}/postulaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public PostulacionResponse postular(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id,
            @Valid @RequestBody PostulacionRequest request) {
        Usuario usuario = autenticarComo(authorization, request.usuarioId());
        return PostulacionResponse.fromDomain(scrims.postular(id, usuario, request.rolDeseado()));
    }

    /**
     * Confirma la asistencia del usuario autenticado a un scrim con lobby armado.
     */
    @PostMapping("/{id}/confirmaciones")
    public ConfirmacionResponse confirmar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmacionRequest request) {
        Usuario usuario = autenticarComo(authorization, request.usuarioId());
        return ConfirmacionResponse.fromDomain(scrims.confirmar(id, usuario));
    }

    /**
     * Ejecuta una acción del organizador sobre el lobby (Patrón Command).
     *
     * @param command {@code asignar-rol}, {@code swap}, {@code invitar} o {@code deshacer}
     */
    @PostMapping("/{id}/acciones/{command}")
    public AccionScrimResponse ejecutarAccion(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id,
            @PathVariable String command,
            @Valid @RequestBody AccionScrimRequest request) {
        Usuario organizador = autenticarComo(authorization, request.organizadorId());
        Usuario principal = request.jugadorPrincipalId() != null ? auth.buscarUsuario(request.jugadorPrincipalId()) : null;
        Usuario secundario = request.jugadorSecundarioId() != null ? auth.buscarUsuario(request.jugadorSecundarioId()) : null;

        String resultado = scrims.ejecutarAccion(id, organizador, command, principal, secundario,
                request.nuevoRol(), request.ladoDestino());
        return new AccionScrimResponse(command, resultado, ScrimResponse.fromDomain(scrims.obtener(id)));
    }

    /**
     * Cancela un scrim del organizador autenticado antes de su inicio.
     */
    @PostMapping("/{id}/cancelar")
    public ScrimResponse cancelar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelarScrimRequest request) {
        Usuario solicitante = auth.autenticar(authorization);
        return ScrimResponse.fromDomain(scrims.cancelar(id, solicitante, request != null ? request.motivo() : null));
    }

    /**
     * Finaliza un scrim en juego del organizador autenticado.
     */
    @PostMapping("/{id}/finalizar")
    public ScrimResponse finalizar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) FinalizarScrimRequest request) {
        Usuario solicitante = auth.autenticar(authorization);
        ResultadoPartida resultado = request != null && (request.ganador() != null || request.marcador() != null)
                ? new ResultadoPartida(request.ganador(), request.marcador())
                : null;
        return ScrimResponse.fromDomain(scrims.finalizar(id, solicitante, resultado));
    }

    /**
     * Carga las estadísticas de un jugador en un scrim finalizado. Solo el organizador.
     */
    @PostMapping("/{id}/estadisticas")
    @ResponseStatus(HttpStatus.CREATED)
    public EstadisticaResponse registrarEstadistica(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id,
            @Valid @RequestBody EstadisticaRequest request) {
        Usuario solicitante = auth.autenticar(authorization);
        Usuario jugador = auth.buscarUsuario(request.usuarioId());
        return EstadisticaResponse.fromDomain(scrims.registrarEstadistica(id, solicitante, jugador,
                request.mvp(), request.kda(), request.observaciones()));
    }

    private Usuario autenticarComo(String authorization, UUID idDeclarado) {
        Usuario usuario = auth.autenticar(authorization);
        auth.exigirIdentidad(usuario, idDeclarado);
        return usuario;
    }
}
