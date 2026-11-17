package com.escrims.service;

import com.escrims.domain.builder.ScrimBuilder;
import com.escrims.domain.command.AsignarRolCommand;
import com.escrims.domain.command.InvitarJugadorCommand;
import com.escrims.domain.command.ScrimCommand;
import com.escrims.domain.command.ScrimCommandInvoker;
import com.escrims.domain.command.SwapJugadoresCommand;
import com.escrims.domain.entities.Confirmacion;
import com.escrims.domain.entities.DatosScrim;
import com.escrims.domain.entities.Estadistica;
import com.escrims.domain.entities.GestorLobbyScrim;
import com.escrims.domain.entities.LadoEquipo;
import com.escrims.domain.entities.Modalidad;
import com.escrims.domain.entities.Postulacion;
import com.escrims.domain.entities.ResultadoPartida;
import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Scrim;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.observer.DomainEvent;
import com.escrims.domain.observer.DomainEventBus;
import com.escrims.domain.observer.ScrimCreadoEvent;
import com.escrims.domain.observer.ScrimStateChanged;
import com.escrims.domain.observer.Subscriber;
import com.escrims.domain.repository.ScrimRepository;
import com.escrims.domain.state.EnJuegoState;
import com.escrims.domain.state.FinalizadoState;
import com.escrims.domain.state.ScrimContext;
import com.escrims.service.exception.AccesoDenegadoException;
import com.escrims.service.exception.NoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de aplicación que coordina la creación y el ciclo de vida de los
 * scrims mediante los patrones Builder, State, Repository, Command y Observer.
 *
 * <p>Los métodos son sincronizados porque los agregados de dominio no son
 * seguros ante concurrencia y el scheduler los modifica desde otro hilo.</p>
 */
@Service
public class ScrimService implements Subscriber {

    private static final Logger log = LoggerFactory.getLogger(ScrimService.class);
    private static final Pattern FORMATO_KDA = Pattern.compile("\\s*(\\d{1,4})\\s*/\\s*(\\d{1,4})\\s*/\\s*(\\d{1,4})\\s*");

    /** Nombres de comando aceptados por {@code POST /api/scrims/{id}/acciones/{command}}. */
    public static final String CMD_ASIGNAR_ROL = "asignar-rol";
    public static final String CMD_SWAP = "swap";
    public static final String CMD_INVITAR = "invitar";
    public static final String CMD_DESHACER = "deshacer";

    private final ScrimRepository scrims;
    private final DomainEventBus eventBus;
    private final Map<UUID, GestorLobbyScrim> lobbies = new HashMap<>();
    private final Map<UUID, ScrimCommandInvoker> invokers = new HashMap<>();
    private final Map<UUID, Map<UUID, Estadistica>> estadisticas = new HashMap<>();
    private final Map<UUID, ResultadoPartida> resultados = new HashMap<>();
    private final Set<UUID> scrimsContabilizados = new HashSet<>();

    /**
     * Inicializa el servicio y lo suscribe al bus de eventos para llevar
     * el historial de scrims jugados de cada participante.
     *
     * @param scrims   repositorio de scrims
     * @param eventBus bus de eventos de dominio
     */
    public ScrimService(ScrimRepository scrims, DomainEventBus eventBus) {
        this.scrims = scrims;
        this.eventBus = eventBus;
        eventBus.subscribe(this);
    }

    /**
     * Construye y persiste un nuevo scrim, delegando las validaciones al Builder (RN-01 a RN-08).
     *
     * @param organizador     usuario responsable del encuentro
     * @param datos           juego, formato, región, rangos, fecha y latencia máxima
     * @param duracionMinutos duración estimada, o null para el valor por defecto
     * @param modalidad       modalidad de juego, o null para el valor por defecto
     * @param reglasRoles     cupos requeridos por rol, o null si no hay reglas
     * @return scrim creado, en estado "Buscando jugadores"
     * @throws IllegalStateException    si el organizador no puede crear scrims (email sin verificar o cooldown activo)
     * @throws IllegalArgumentException si la configuración incumple alguna regla
     */
    public synchronized Scrim crear(Usuario organizador, DatosScrim datos, Integer duracionMinutos,
                                    Modalidad modalidad, Map<RolJuego, Integer> reglasRoles) {
        ScrimBuilder builder = new ScrimBuilder()
                .organizador(organizador)
                .juego(datos.getJuego())
                .formato(datos.getFormato())
                .region(datos.getRegion())
                .rango(datos.getRangoMin(), datos.getRangoMax())
                .fechaHora(datos.getInicio());
        if (datos.getLatenciaMaxima() > 0) {
            builder.latenciaMax(datos.getLatenciaMaxima());
        }
        if (duracionMinutos != null) {
            builder.duracion(duracionMinutos);
        }
        if (modalidad != null) {
            builder.modalidad(modalidad);
        }
        if (reglasRoles != null) {
            reglasRoles.forEach(builder::agregarReglaRol);
        }

        Scrim scrim = builder.build();
        scrims.guardar(scrim);
        lobbies.put(scrim.getId(), new GestorLobbyScrim(scrim));
        log.info("[AUDITORIA] Scrim {} creado por {}.", scrim.getId(), organizador.getId());
        eventBus.publish(new ScrimCreadoEvent(scrim.getId()));
        return scrim;
    }

    /**
     * Busca los scrims que siguen buscando jugadores.
     *
     * @param filtro criterios opcionales; los campos nulos no restringen
     */
    public List<Scrim> buscar(DatosScrim filtro) {
        return scrims.buscarDisponibles(filtro);
    }

    /**
     * Obtiene un scrim por su identificador.
     *
     * @throws NoEncontradoException si no existe
     */
    public Scrim obtener(UUID scrimId) {
        return scrims.buscarPorId(scrimId)
                .orElseThrow(() -> new NoEncontradoException("Scrim no encontrado: " + scrimId));
    }

    /**
     * Postula a un jugador en un scrim y lo incorpora al lobby. Cuando se
     * completan los cupos el scrim pasa a "Lobby armado" (RN-08).
     *
     * @return postulación aceptada del jugador
     * @throws IllegalStateException    si el estado del scrim o del jugador (email, cooldown) lo impide
     * @throws IllegalArgumentException si el rango del jugador está fuera del permitido
     */
    public synchronized Postulacion postular(UUID scrimId, Usuario usuario, RolJuego rol) {
        Scrim scrim = obtener(scrimId);
        ScrimContext ctx = new ScrimContext(scrim);
        String estadoAnterior = ctx.getState().getNombreEstado();

        ctx.postular(usuario, rol);
        GestorLobbyScrim lobby = lobbyDe(scrim);
        if (!lobby.esTitular(usuario) && !lobby.estaEnListaEspera(usuario)) {
            lobby.incorporarJugador(usuario, rol);
        }
        registrarCambio(scrim, estadoAnterior);

        return scrim.getPostulaciones().stream()
                .filter(p -> p.getUsuario().equals(usuario))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se pudo registrar la postulación."));
    }

    /**
     * Confirma la asistencia de un jugador del lobby. Con el 100% de
     * confirmaciones el scrim pasa a "Confirmado" (RN-09).
     *
     * @return confirmación del jugador
     * @throws IllegalStateException si el estado no admite confirmaciones o el jugador no integra el lobby
     */
    public synchronized Confirmacion confirmar(UUID scrimId, Usuario usuario) {
        Scrim scrim = obtener(scrimId);
        ScrimContext ctx = new ScrimContext(scrim);
        String estadoAnterior = ctx.getState().getNombreEstado();

        ctx.confirmar(usuario);
        registrarCambio(scrim, estadoAnterior);

        return scrim.getConfirmaciones().stream()
                .filter(c -> c.getUsuario().equals(usuario))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se pudo registrar la confirmación."));
    }

    /**
     * Ejecuta una acción administrativa del organizador sobre el lobby
     * mediante el patrón Command (RN-12).
     *
     * @param comando    {@value #CMD_ASIGNAR_ROL}, {@value #CMD_SWAP}, {@value #CMD_INVITAR} o {@value #CMD_DESHACER}
     * @param principal  jugador objetivo (no aplica a {@value #CMD_DESHACER})
     * @param secundario segundo jugador, solo para {@value #CMD_SWAP}
     * @param rol        rol a asignar, para {@value #CMD_ASIGNAR_ROL} e {@value #CMD_INVITAR}
     * @param lado       equipo de destino, solo para {@value #CMD_INVITAR}
     * @return descripción de la acción realizada
     * @throws AccesoDenegadoException  si el solicitante no es el organizador
     * @throws IllegalArgumentException si el comando no existe o le faltan parámetros
     * @throws IllegalStateException    si el lobby o el estado del scrim no admiten la acción
     */
    public synchronized String ejecutarAccion(UUID scrimId, Usuario solicitante, String comando,
                                              Usuario principal, Usuario secundario,
                                              RolJuego rol, LadoEquipo lado) {
        Scrim scrim = obtener(scrimId);
        exigirOrganizador(scrim, solicitante);
        GestorLobbyScrim lobby = lobbyDe(scrim);
        ScrimCommandInvoker invoker = invokers.computeIfAbsent(scrimId, id -> new ScrimCommandInvoker());

        String nombre = comando == null ? "" : comando.trim().toLowerCase();
        String descripcion;
        switch (nombre) {
            case CMD_DESHACER -> {
                if (!invoker.deshacerUltimo()) {
                    throw new IllegalStateException("No hay acciones para deshacer en este scrim.");
                }
                descripcion = "Se deshizo la última acción.";
            }
            case CMD_ASIGNAR_ROL -> {
                ejecutar(invoker, new AsignarRolCommand(lobby, principal, rol));
                descripcion = "Rol " + rol + " asignado a " + principal.getUsername() + ".";
            }
            case CMD_SWAP -> {
                ejecutar(invoker, new SwapJugadoresCommand(lobby, principal, secundario));
                descripcion = "Intercambio entre " + principal.getUsername() + " y " + secundario.getUsername() + ".";
            }
            case CMD_INVITAR -> {
                if (principal != null && principal.tieneCooldownActivo()) {
                    throw new IllegalStateException("El jugador invitado posee una sanción/cooldown activo.");
                }
                ejecutar(invoker, new InvitarJugadorCommand(lobby, principal, rol, lado));
                descripcion = principal.getUsername() + " invitado a " + lado + " como " + rol + ".";
            }
            default -> throw new IllegalArgumentException("Comando desconocido: '" + comando
                    + "'. Válidos: " + CMD_ASIGNAR_ROL + ", " + CMD_SWAP + ", " + CMD_INVITAR + ", " + CMD_DESHACER + ".");
        }
        log.info("[AUDITORIA] Scrim {}: comando '{}' ejecutado por {}. {}", scrimId, nombre, solicitante.getId(), descripcion);
        return descripcion;
    }

    /**
     * Cancela un scrim antes de su inicio (RN-12).
     *
     * @throws AccesoDenegadoException si el solicitante no es el organizador
     * @throws IllegalStateException   si el scrim ya está en juego, finalizado o cancelado
     */
    public synchronized Scrim cancelar(UUID scrimId, Usuario solicitante, String motivo) {
        Scrim scrim = obtener(scrimId);
        exigirOrganizador(scrim, solicitante);
        ScrimContext ctx = new ScrimContext(scrim);
        String estadoAnterior = ctx.getState().getNombreEstado();

        ctx.cancelar();
        log.info("[AUDITORIA] Scrim {} cancelado por {}. Motivo: {}", scrimId, solicitante.getId(),
                (motivo == null || motivo.isBlank()) ? "(sin motivo)" : motivo);
        registrarCambio(scrim, estadoAnterior);
        return scrim;
    }

    /**
     * Finaliza un scrim en juego y guarda su resultado.
     *
     * @throws AccesoDenegadoException si el solicitante no es el organizador
     * @throws IllegalStateException   si el scrim no está en juego
     */
    public synchronized Scrim finalizar(UUID scrimId, Usuario solicitante, ResultadoPartida resultado) {
        Scrim scrim = obtener(scrimId);
        exigirOrganizador(scrim, solicitante);
        ScrimContext ctx = new ScrimContext(scrim);
        String estadoAnterior = ctx.getState().getNombreEstado();

        ctx.finalizar();
        if (resultado != null) {
            resultados.put(scrimId, resultado);
        }
        registrarCambio(scrim, estadoAnterior);
        return scrim;
    }

    /**
     * Carga las estadísticas individuales de un jugador en un scrim finalizado (RN-13).
     *
     * @param kda texto con formato {@code kills/deaths/assists}, por ejemplo {@code "18/6/4"}
     * @throws AccesoDenegadoException  si el solicitante no es el organizador
     * @throws IllegalStateException    si el scrim no finalizó o el jugador ya tiene estadísticas cargadas
     * @throws IllegalArgumentException si el jugador no participó o el KDA tiene un formato inválido
     */
    public synchronized Estadistica registrarEstadistica(UUID scrimId, Usuario solicitante, Usuario jugador,
                                                         boolean mvp, String kda, String observaciones) {
        Scrim scrim = obtener(scrimId);
        exigirOrganizador(scrim, solicitante);
        if (!(scrim.getEstado() instanceof FinalizadoState)) {
            throw new IllegalStateException("Las estadísticas se cargan cuando el scrim está finalizado.");
        }
        if (!scrim.esParticipante(jugador)) {
            throw new IllegalArgumentException("El jugador indicado no participó de este scrim.");
        }
        Matcher m = FORMATO_KDA.matcher(kda == null ? "" : kda);
        if (!m.matches()) {
            throw new IllegalArgumentException("El KDA debe tener el formato kills/deaths/assists (ej. '18/6/4').");
        }
        Map<UUID, Estadistica> delScrim = estadisticas.computeIfAbsent(scrimId, id -> new LinkedHashMap<>());
        if (delScrim.containsKey(jugador.getId())) {
            throw new IllegalStateException("Ya se cargaron las estadísticas de este jugador para el scrim.");
        }
        Estadistica estadistica = new Estadistica(scrim, jugador, Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), mvp, observaciones);
        delScrim.put(jugador.getId(), estadistica);
        return estadistica;
    }

    /**
     * Obtiene el resultado cargado al finalizar el scrim, o null si no se informó.
     */
    public synchronized ResultadoPartida resultadoDe(UUID scrimId) {
        return resultados.get(scrimId);
    }

    /**
     * Al iniciarse un scrim suma la partida al historial de cada participante,
     * sin importar si la transición la hizo este servicio o el scheduler.
     */
    @Override
    public synchronized void onEvent(DomainEvent event) {
        if (!(event instanceof ScrimStateChanged cambio)) {
            return;
        }
        scrims.buscarPorId(cambio.scrimId()).ifPresent(scrim -> {
            boolean iniciado = scrim.getEstado() instanceof EnJuegoState || scrim.getEstado() instanceof FinalizadoState;
            if (iniciado && scrimsContabilizados.add(scrim.getId())) {
                scrim.getConfirmaciones().stream()
                        .filter(Confirmacion::isConfirmado)
                        .forEach(c -> c.getUsuario().registrarScrimJugado());
            }
        });
    }

    private void ejecutar(ScrimCommandInvoker invoker, ScrimCommand command) {
        invoker.ejecutar(command);
    }

    private GestorLobbyScrim lobbyDe(Scrim scrim) {
        return lobbies.computeIfAbsent(scrim.getId(), id -> new GestorLobbyScrim(scrim));
    }

    private void exigirOrganizador(Scrim scrim, Usuario solicitante) {
        if (!solicitante.equals(scrim.getOrganizador())) {
            throw new AccesoDenegadoException("Solo el organizador del scrim puede realizar esta acción.");
        }
    }

    private void registrarCambio(Scrim scrim, String estadoAnterior) {
        scrims.guardar(scrim);
        String nuevoEstado = scrim.getEstado().getNombreEstado();
        if (!nuevoEstado.equals(estadoAnterior)) {
            log.info("[AUDITORIA] Scrim {}: {} -> {}", scrim.getId(), estadoAnterior, nuevoEstado);
            eventBus.publish(new ScrimStateChanged(scrim.getId(), estadoAnterior, nuevoEstado));
        }
    }
}
