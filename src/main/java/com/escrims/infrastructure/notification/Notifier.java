package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.ResultadoEnvio;

/**
 * Define el contrato AbstractProduct del patrón Abstract Factory y Target
 * del patrón Adapter para el envío de notificaciones por distintos canales.
 */
public interface Notifier {

    /**
     * Envía una notificación al destinatario mediante el canal implementado.
     *
     * @param mensaje contenido de la notificación que se desea enviar
     * @param destinatario identificador del destinatario de la notificación
     * @return resultado del envío, indicando su éxito y cantidad de intentos
     */
    ResultadoEnvio enviar(String mensaje, String destinatario);

    /**
     * Solicita el reintento del envío de una notificación según
     * el comportamiento definido por la implementación concreta.
     *
     * @param maxIntentos cantidad máxima de reintentos solicitados
     * @return resultado del reintento, indicando su éxito y cantidad de intentos
     */
    ResultadoEnvio reintentar(int maxIntentos);
}
