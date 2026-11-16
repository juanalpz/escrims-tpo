package com.escrims.controller.dto;

import com.escrims.domain.entities.Confirmacion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida de una confirmación de asistencia junto con el estado en que quedó el scrim (RN-09).
 *
 * @param id                Identificador de la confirmación.
 * @param usuarioId         Jugador que confirmó.
 * @param confirmado        Indica si la asistencia quedó confirmada.
 * @param fechaConfirmacion Momento de la confirmación.
 * @param scrim             Resumen actualizado del scrim.
 */
public record ConfirmacionResponse(
        UUID id,
        UUID usuarioId,
        boolean confirmado,
        LocalDateTime fechaConfirmacion,
        ScrimResponse scrim) {

    public static ConfirmacionResponse fromDomain(Confirmacion c) {
        return new ConfirmacionResponse(c.getId(), c.getUsuario().getId(), c.isConfirmado(),
                c.getFechaConfirmacion(), ScrimResponse.fromDomain(c.getScrim()));
    }
}
