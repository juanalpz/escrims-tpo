package com.escrims.domain.command;

/**
 * «Command» Contrato para acciones reversibles del organizador sobre un Scrim
 * antes de que comience la partida (RN-12).
 */
public interface ScrimCommand {
    void execute();

    void undo();
}
