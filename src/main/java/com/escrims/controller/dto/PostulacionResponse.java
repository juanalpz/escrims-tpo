package com.escrims.controller.dto;

import com.escrims.domain.entities.EstadoPostulacion;
import com.escrims.domain.entities.Postulacion;
import com.escrims.domain.entities.RolJuego;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida de una postulación junto con el estado en que quedó el scrim.
 *
 * @param id               Identificador de la postulación.
 * @param usuarioId        Jugador postulado.
 * @param rolDeseado       Rol solicitado.
 * @param estado           Estado de la postulación.
 * @param fechaPostulacion Momento de la postulación.
 * @param scrim            Resumen actualizado del scrim.
 */
public record PostulacionResponse(
        UUID id,
        UUID usuarioId,
        RolJuego rolDeseado,
        EstadoPostulacion estado,
        LocalDateTime fechaPostulacion,
        ScrimResponse scrim) {

    public static PostulacionResponse fromDomain(Postulacion p) {
        return new PostulacionResponse(p.getId(), p.getUsuario().getId(), p.getRolDeseado(), p.getEstado(),
                p.getFechaPostulacion(), ScrimResponse.fromDomain(p.getScrim()));
    }
}
