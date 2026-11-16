package com.escrims.domain.state;

import com.escrims.domain.entities.*;

/**
 * Implementa el Context del patrón State, administrando el estado actual
 * del scrim y delegando las operaciones a su implementación activa.
 */
public class ScrimContext {

    private final Scrim scrim;
    private ScrimState state;

    /**
     * Inicializa el contexto con el estado del scrim o con el estado
     * BuscandoState si no existe uno previamente asignado.
     *
     * @param scrim scrim cuyo ciclo de vida se desea administrar
     * @throws IllegalArgumentException si el scrim es nulo
     */
    public ScrimContext(Scrim scrim) {
        if (scrim == null) {
            throw new IllegalArgumentException("El Scrim no puede ser nulo en el contexto.");
        }
        this.scrim = scrim;
        this.state = (scrim.getEstado() != null) ? scrim.getEstado() : new BuscandoState();
        this.scrim.setEstado(this.state);
    }

    /**
     * Actualiza el estado del contexto y lo sincroniza con la entidad Scrim.
     *
     * @param nuevoEstado nuevo estado del ciclo de vida del scrim
     * @throws IllegalArgumentException si el nuevo estado es nulo
     */
    public void setState(ScrimState nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo.");
        }
        this.state = nuevoEstado;
        this.scrim.setEstado(nuevoEstado);
    }

    /**
     * Delega la postulación de un jugador al estado actual del scrim.
     *
     * @param usuario jugador que solicita incorporarse
     * @param rol rol de juego solicitado
     * @throws IllegalArgumentException si los datos de postulación
     *         incumplen las validaciones del estado actual
     * @throws IllegalStateException si el estado actual no permite
     *         postularse o se incumplen los requisitos de participación
     */
    public void postular(Usuario usuario, RolJuego rol) {
        this.state.postular(this, usuario, rol);
    }

    /**
     * Delega la confirmación de participación al estado actual del scrim.
     *
     * @param usuario jugador que desea confirmar su participación
     * @throws IllegalArgumentException si el usuario es nulo
     *         y el estado actual requiere su identificación
     * @throws IllegalStateException si el estado actual no permite
     *         confirmar o el jugador no cumple las condiciones necesarias
     */
    public void confirmar(Usuario usuario) {
        this.state.confirmar(this, usuario);
    }

    /**
     * Delega el inicio de la partida al estado actual del scrim.
     *
     * @throws IllegalStateException si el estado actual no permite
     *         iniciar la partida
     */
    public void iniciar() {
        this.state.iniciar(this);
    }

    /**
     * Delega la finalización de la partida al estado actual del scrim.
     *
     * @throws IllegalStateException si el estado actual no permite
     *         finalizar la partida
     */
    public void finalizar() {
        this.state.finalizar(this);
    }

    /**
     * Delega la cancelación de la partida al estado actual del scrim.
     *
     * @throws IllegalStateException si el estado actual no permite
     *         cancelar la partida
     */
    public void cancelar() {
        this.state.cancelar(this);
    }

    /**
     * Obtiene el scrim administrado por el contexto.
     *
     * @return entidad Scrim asociada
     */
    public Scrim getScrim() {
        return scrim;
    }

    /**
     * Obtiene el estado actual del ciclo de vida del scrim.
     *
     * @return implementación activa de ScrimState
     */
    public ScrimState getState() {
        return state;
    }

    /**
     * Verifica si las postulaciones aceptadas alcanzaron la capacidad
     * máxima de participantes del scrim.
     *
     * @return true si las postulaciones aceptadas alcanzan o superan
     *         los cupos disponibles; false en caso contrario
     */
    public boolean estaCupoCompleto() {
        long aceptados = scrim.getPostulaciones().stream()
                .filter(p -> p.getEstado() == EstadoPostulacion.ACEPTADA)
                .count();
        return aceptados >= scrim.getCupos();
    }

    /**
     * Verifica si todos los participantes del lobby confirmaron
     * su asistencia y se alcanzó la cantidad de cupos requerida (RN-09).
     *
     * @return true si existen confirmaciones para todos los cupos
     *         y todas están confirmadas; false en caso contrario
     */
    public boolean todosConfirmaron() {
        if (scrim.getConfirmaciones().isEmpty()) {
            return false;
        }
        return scrim.getConfirmaciones().size() == scrim.getCupos()
                && scrim.getConfirmaciones().stream().allMatch(Confirmacion::isConfirmado);
    }
}
