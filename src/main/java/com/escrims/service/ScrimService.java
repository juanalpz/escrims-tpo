package com.escrims.service;

import com.escrims.domain.builder.ScrimBuilder;
import com.escrims.domain.command.ScrimCommandInvoker;
import com.escrims.domain.entities.*;
import com.escrims.domain.repository.ScrimRepository;
import com.escrims.domain.state.ScrimContext;

import java.util.UUID;

/**
 * Servicio de aplicación que coordina la creación y gestión del ciclo de vida
 * de los scrims mediante los patrones Builder, State, Repository y Command.
 */
public class ScrimService {

    private final MatchmakingService matchmaking;
    private final ScrimRepository scrims;
    private final ScrimCommandInvoker invoker;
    private final ScrimBuilder builder;

    /**
     * Inicializa el servicio con sus dependencias de matchmaking,
     * persistencia, ejecución de comandos y construcción de scrims.
     *
     * @param matchmaking servicio encargado del emparejamiento de jugadores
     * @param scrims repositorio utilizado para consultar y persistir scrims
     * @param invoker ejecutor de comandos asociados a los scrims
     * @param builder constructor de scrims mediante el patrón Builder
     */
    public ScrimService(MatchmakingService matchmaking, ScrimRepository scrims,
                        ScrimCommandInvoker invoker, ScrimBuilder builder) {
        this.matchmaking = matchmaking;
        this.scrims = scrims;
        this.invoker = invoker;
        this.builder = builder;
    }

    /**
     * Construye y persiste un nuevo scrim a partir de su configuración
     * y organizador, delegando las validaciones al Builder (RN-01 a RN-08).
     *
     * @param datos configuración y requisitos del scrim
     * @param organizador usuario responsable de organizar el encuentro
     * @return instancia del scrim creado y persistido
     */
    public Scrim crear(DatosScrim datos, Usuario organizador) {
        Scrim scrim = builder
                .organizador(organizador)
                .juego(datos.getJuego())
                .formato(datos.getFormato())
                .region(datos.getRegion())
                .rango(datos.getRangoMin(), datos.getRangoMax())
                .fechaHora(datos.getInicio())
                .build();
        scrims.guardar(scrim);
        return scrim;
    }

    /**
     * Define la operación de postulación de un usuario a un scrim,
     * pendiente de implementación mediante ScrimContext.
     *
     * @param scrimId identificador del scrim al que se desea postular
     * @param usuarioId identificador del usuario postulante
     * @param rol rol de juego solicitado
     * @return postulación generada, una vez implementada la operación
     * @throws UnsupportedOperationException siempre, porque la operación
     *         todavía no está implementada
     */
    public Postulacion postular(UUID scrimId, UUID usuarioId, String rol) {
        // Delegaría a ScrimContext.postular()
        throw new UnsupportedOperationException("Implementar con ScrimContext");
    }

    /**
     * Define la operación de confirmación de participación de un usuario,
     * pendiente de implementación mediante ScrimContext.
     *
     * @param scrimId identificador del scrim que se desea confirmar
     * @param usuarioId identificador del usuario que confirma su participación
     * @return confirmación generada, una vez implementada la operación
     * @throws UnsupportedOperationException siempre, porque la operación
     *         todavía no está implementada
     */
    public Confirmacion confirmar(UUID scrimId, UUID usuarioId) {
        throw new UnsupportedOperationException("Implementar con ScrimContext");
    }

    /**
     * Cancela un scrim existente mediante la transición de estado
     * correspondiente y persiste el cambio (RN-12).
     *
     * @param scrimId identificador del scrim que se desea cancelar
     * @param motivo motivo indicado para la cancelación
     * @throws IllegalArgumentException si no existe un scrim con el identificador indicado
     * @throws IllegalStateException si el estado actual del scrim
     *         no permite su cancelación
     */
    public void cancelar(UUID scrimId, String motivo) {
        Scrim scrim = scrims.buscarPorId(scrimId)
                .orElseThrow(() -> new IllegalArgumentException("Scrim no encontrado: " + scrimId));
        ScrimContext ctx = new ScrimContext(scrim);
        ctx.cancelar();
        scrims.guardar(scrim);
    }

    /**
     * Finaliza un scrim existente mediante la transición de estado
     * correspondiente y persiste el cambio.
     *
     * @param scrimId identificador del scrim que se desea finalizar
     * @param resultado resultado final de la partida
     * @throws IllegalArgumentException si no existe un scrim con el identificador indicado
     * @throws IllegalStateException si el estado actual del scrim
     *         no permite finalizar la partida
     */
    public void finalizar(UUID scrimId, ResultadoPartida resultado) {
        Scrim scrim = scrims.buscarPorId(scrimId)
                .orElseThrow(() -> new IllegalArgumentException("Scrim no encontrado: " + scrimId));
        ScrimContext ctx = new ScrimContext(scrim);
        ctx.finalizar();
        scrims.guardar(scrim);
    }

    /**
     * Obtiene el servicio de matchmaking asociado.
     *
     * @return servicio de emparejamiento configurado
     */
    public MatchmakingService getMatchmaking() { return matchmaking; }

    /**
     * Obtiene el repositorio de scrims utilizado por el servicio.
     *
     * @return repositorio de scrims configurado
     */
    public ScrimRepository getScrims() { return scrims; }

    /**
     * Obtiene el ejecutor de comandos asociado al servicio.
     *
     * @return instancia del ejecutor de comandos
     */
    public ScrimCommandInvoker getInvoker() { return invoker; }
}
