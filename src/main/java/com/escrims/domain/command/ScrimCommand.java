package com.escrims.domain.command;

/**
 * Contrato para acciones administrativas reversibles del organizador antes del inicio de un scrim (Patrón Command, RN-12).
 */
public interface ScrimCommand {

    /**
     * Ejecuta la acción del comando sobre el lobby del scrim.
     *
     * @throws IllegalStateException si el estado del encuentro no permite modificaciones (RN-12).
     */
    void execute();

    /**
     * Deshace la acción previamente ejecutada restaurando el estado anterior del lobby.
     *
     * @throws IllegalStateException si el comando no fue ejecutado o la partida ya inició (RN-12).
     */
    void undo();
}
