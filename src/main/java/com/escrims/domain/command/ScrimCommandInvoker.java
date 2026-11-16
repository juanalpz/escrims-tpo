package com.escrims.domain.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Invocador que ejecuta comandos sobre el lobby y mantiene un historial LIFO para revertir acciones secuencialmente (Patrón Command, RN-12).
 */
public class ScrimCommandInvoker {

    private final Deque<ScrimCommand> historial = new ArrayDeque<>();

    /**
     * Ejecuta el comando recibido y lo apila en el historial para permitir su posterior reversión.
     *
     * @param command comando a ejecutar sobre el lobby del scrim.
     * @throws IllegalArgumentException si el comando recibido es nulo.
     */
    public void ejecutar(ScrimCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo.");
        }
        command.execute();
        historial.push(command);
    }

    /**
     * Deshace el último comando ejecutado retirándolo del historial LIFO.
     *
     * @return {@code true} si se revirtió un comando, o {@code false} si el historial estaba vacío.
     */
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
