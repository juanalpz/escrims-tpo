package com.escrims.controller.dto;

import com.escrims.domain.entities.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida con el resumen y estado actual de un scrim para la API REST.
 *
 * @param id                     Identificador único del scrim.
 * @param juego                  Videojuego del encuentro.
 * @param formato                Formato de equipos (ej. 5v5, 3v3, 1v1).
 * @param region                 Región o servidor donde se disputa la partida.
 * @param rangoMin               Rango mínimo admitido.
 * @param rangoMax               Rango máximo admitido.
 * @param latenciaMax            Latencia máxima permitida en milisegundos.
 * @param fechaHora              Fecha y hora programada de inicio.
 * @param cuposTotales           Cantidad total de plazas requeridas para el encuentro.
 * @param postulacionesAceptadas Cantidad de jugadores actualmente aceptados en el lobby.
 * @param estadoActual           Nombre legible del estado activo del scrim.
 */
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

    /**
     * Convierte una entidad de dominio {@link Scrim} en su representación de salida para la API.
     *
     * @param scrim entidad de dominio del encuentro a mapear.
     * @return instancia de {@link ScrimResponse} con el resumen y conteo de cupos ocupados.
     */
    public static ScrimResponse fromDomain(Scrim scrim) {
        // Contabiliza únicamente las postulaciones en estado ACEPTADA
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
