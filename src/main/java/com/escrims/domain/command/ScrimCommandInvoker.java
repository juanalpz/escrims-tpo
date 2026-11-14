package com.escrims.domain.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Invocador que ejecuta comandos sobre el lobby y mantiene un historial
 * LIFO para soportar undo() secuencial.
 */
public class ScrimCommandInvoker {

    private final Deque<ScrimCommand> historial = new ArrayDeque<>();

    public void ejecutar(ScrimCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo.");
        }
        command.execute();
        historial.push(command);
    }

    public boolean deshacerUltimo() {
        if (historial.isEmpty()) {
            return false;
        }
        ScrimCommand ultimo = historial.pop();
        ultimo.undo();
        return true;
    }

    public int getCantidadComandosEnHistorial() {
        return historial.size();
    }
}
