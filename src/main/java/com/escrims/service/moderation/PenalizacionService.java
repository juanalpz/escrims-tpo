package com.escrims.service.moderation;

import com.escrims.domain.entities.Confirmacion;
import com.escrims.domain.entities.RegistroIncidencia;
import com.escrims.domain.entities.Sancion;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.TipoInfraccion;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.moderation.PoliticaSanciones;
import com.escrims.domain.observer.DomainEventBus;
import com.escrims.domain.observer.SancionAplicadaEvent;
import com.escrims.domain.repository.RegistroIncidenciaRepository;
import com.escrims.domain.repository.ScrimRepository;
import com.escrims.domain.state.EnJuegoState;
import com.escrims.domain.state.FinalizadoState;
import com.escrims.service.AuthService;
import com.escrims.service.exception.AccesoDenegadoException;
import com.escrims.service.exception.NoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación del sistema de penalidades: registra abandonos y
 * no-shows y aplica el strike con cooldown que define {@link PoliticaSanciones}.
 */
@Service
public class PenalizacionService {

    private static final Logger log = LoggerFactory.getLogger(PenalizacionService.class);

    private final ScrimRepository scrims;
    private final RegistroIncidenciaRepository incidencias;
    private final AuthService auth;
    private final DomainEventBus eventBus;
    private final PoliticaSanciones politica;

    public PenalizacionService(ScrimRepository scrims, RegistroIncidenciaRepository incidencias,
                               AuthService auth, DomainEventBus eventBus, PoliticaSanciones politica) {
        this.scrims = scrims;
        this.incidencias = incidencias;
        this.auth = auth;
        this.eventBus = eventBus;
        this.politica = politica;
    }

    /**
     * Registra el abandono o no-show de un jugador en un scrim ya iniciado
     * y le aplica la sanción correspondiente.
     *
     * <p>Pueden registrarlo el organizador del scrim o un moderador; un
     * jugador también puede declarar su propio abandono.</p>
     *
     * @param scrimId     scrim en el que ocurrió la incidencia
     * @param solicitante usuario autenticado que la registra
     * @param jugador     jugador que abandonó o no se presentó
     * @param tipo        {@link TipoInfraccion#ABANDONO} o {@link TipoInfraccion#NO_SHOW}
     * @param detalle     observación opcional
     * @return incidencia registrada con su sanción
     * @throws NoEncontradoException    si el scrim no existe
     * @throws AccesoDenegadoException  si el solicitante no está habilitado
     * @throws IllegalStateException    si el scrim no comenzó o la incidencia ya estaba registrada
     * @throws IllegalArgumentException si el jugador no participa del scrim
     */
    public synchronized RegistroIncidencia registrarIncidencia(UUID scrimId, Usuario solicitante, Usuario jugador,
                                                               TipoInfraccion tipo, String detalle) {
        Scrim scrim = scrims.buscarPorId(scrimId)
                .orElseThrow(() -> new NoEncontradoException("Scrim no encontrado: " + scrimId));

        boolean autodeclarado = tipo == TipoInfraccion.ABANDONO && solicitante.equals(jugador);
        if (!solicitante.equals(scrim.getOrganizador()) && !auth.esModerador(solicitante) && !autodeclarado) {
            throw new AccesoDenegadoException(
                    "Solo el organizador del scrim o un moderador pueden registrar esta incidencia.");
        }
        if (!(scrim.getEstado() instanceof EnJuegoState) && !(scrim.getEstado() instanceof FinalizadoState)) {
            throw new IllegalStateException(
                    "Solo se registran abandonos o no-shows de un scrim en juego o finalizado.");
        }
        if (!scrim.esParticipante(jugador)) {
            throw new IllegalArgumentException("El jugador indicado no participa de este scrim.");
        }
        if (incidencias.existe(scrimId, jugador.getId())) {
            throw new IllegalStateException("Ya se registró una incidencia de este jugador en el scrim.");
        }
        return aplicar(scrim, jugador, tipo, detalle, LocalDateTime.now());
    }

    /**
     * Sanciona con no-show a los jugadores de un lobby armado que no
     * confirmaron su asistencia. Lo invoca el scheduler cuando llega la
     * hora del scrim y el lobby sigue sin el 100% de confirmaciones.
     *
     * @param scrim scrim con lobby armado y horario vencido
     * @param ahora momento de referencia para el cooldown
     * @return incidencias generadas, una por cada jugador ausente
     */
    public synchronized List<RegistroIncidencia> registrarNoShowsSinConfirmar(Scrim scrim, LocalDateTime ahora) {
        List<RegistroIncidencia> generadas = new ArrayList<>();
        for (Confirmacion confirmacion : scrim.getConfirmaciones()) {
            Usuario jugador = confirmacion.getUsuario();
            if (!confirmacion.isConfirmado() && !incidencias.existe(scrim.getId(), jugador.getId())) {
                generadas.add(aplicar(scrim, jugador, TipoInfraccion.NO_SHOW,
                        "No confirmó asistencia antes del horario del scrim.", ahora));
            }
        }
        return generadas;
    }

    /**
     * Obtiene los abandonos y no-shows registrados de un usuario.
     */
    public List<RegistroIncidencia> incidenciasDe(UUID usuarioId) {
        return incidencias.buscarPorUsuario(usuarioId);
    }

    private RegistroIncidencia aplicar(Scrim scrim, Usuario jugador, TipoInfraccion tipo,
                                       String detalle, LocalDateTime ahora) {
        Sancion sancion = politica.aplicar(jugador, tipo, detalle, ahora);
        if (tipo == TipoInfraccion.ABANDONO) {
            jugador.registrarAbandono();
        } else {
            jugador.registrarNoShow();
        }
        RegistroIncidencia incidencia = new RegistroIncidencia(scrim, jugador, tipo, detalle, sancion);
        incidencias.guardar(incidencia);

        log.info("[AUDITORIA] {} de usuario {} en scrim {}: strikes={}, cooldownHasta={}",
                tipo, jugador.getId(), scrim.getId(), jugador.getStrikes(), sancion.getCooldownHasta());
        eventBus.publish(new SancionAplicadaEvent(jugador.getId(), scrim.getId(), tipo,
                jugador.getStrikes(), sancion.getCooldownHasta()));
        return incidencia;
    }
}
