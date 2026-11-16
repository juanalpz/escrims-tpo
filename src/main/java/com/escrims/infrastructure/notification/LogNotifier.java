package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.ResultadoEnvio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementa un ConcreteProduct del patrón Abstract Factory que simula
 * el envío de notificaciones mediante logs en entornos de desarrollo y pruebas.
 */
public class LogNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(LogNotifier.class);

    private String ultimoMensaje;
    private String ultimoDestinatario;

    /**
     * Registra el envío de una notificación en el log y almacena
     * los datos del último mensaje procesado.
     *
     * @param mensaje contenido de la notificación que se desea enviar
     * @param destinatario destinatario de la notificación
     * @return resultado exitoso simulado con un intento registrado
     */
    @Override
    public ResultadoEnvio enviar(String mensaje, String destinatario) {
        log.info("[LOG-NOTIFIER] Enviando a {}: {}", destinatario, mensaje);
        this.ultimoMensaje = mensaje;
        this.ultimoDestinatario = destinatario;
        return ResultadoEnvio.exito(1);
    }

    /**
     * Registra una solicitud de reintento sin realizar un nuevo envío.
     *
     * @param maxIntentos cantidad máxima de reintentos solicitados
     * @return resultado exitoso simulado con un intento registrado
     */
    @Override
    public ResultadoEnvio reintentar(int maxIntentos) {
        log.info("[LOG-NOTIFIER] Reintento ({} intentos máx.)", maxIntentos);
        return ResultadoEnvio.exito(1);
    }

    /**
     * Obtiene el contenido del último mensaje procesado.
     *
     * @return último mensaje registrado o null si no hubo envíos
     */
    public String getUltimoMensaje() { return ultimoMensaje; }

    /**
     * Obtiene el destinatario de la última notificación procesada.
     *
     * @return último destinatario registrado o null si no hubo envíos
     */
    public String getUltimoDestinatario() { return ultimoDestinatario; }
}
