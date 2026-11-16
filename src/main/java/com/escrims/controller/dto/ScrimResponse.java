package com.escrims.controller.dto;

import com.escrims.domain.entities.*;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScrimResponse(
        UUID id,
        Juego juego,
        Formato formato,
        Region region,
        Rango rangoMin,
        Rango rangoMax,
        int latenciaMax,
        LocalDateTime fechaHora,
        int cuposTotales,
        int postulacionesAceptadas,
        String estadoActual) {
    public static ScrimResponse fromDomain(Scrim scrim) {
        int aceptadas = (int) scrim.getPostulaciones().stream()
                .filter(p -> p.getEstado() == EstadoPostulacion.ACEPTADA)
                .count();
        String nombreEstado = (scrim.getEstado() != null)
                ? scrim.getEstado().getNombreEstado()
                : "SIN_ESTADO";

        return new ScrimResponse(
                scrim.getId(),
                scrim.getJuego(),
                scrim.getFormato(),
                scrim.getRegion(),
                scrim.getRangoMin(),
                scrim.getRangoMax(),
                scrim.getLatenciaMax(),
                scrim.getFechaHora(),
                scrim.getCupos(),
                aceptadas,
                nombreEstado);
    }
}
