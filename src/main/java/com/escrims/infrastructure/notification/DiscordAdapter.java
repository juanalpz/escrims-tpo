package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.ResultadoEnvio;

/**
 * Implementa el patrón Adapter para integrar {@link DiscordClient}
 * con el contrato de notificaciones {@link Notifier}.
 */
public class DiscordAdapter implements Notifier {

    private final DiscordClient cliente;
    private String ultimoMensaje;
    private String ultimoCanal;

    /**
     * Inicializa el adaptador con el cliente de Discord.
     *
     * @param cliente cliente encargado de publicar mensajes en Discord
     */
    public DiscordAdapter(DiscordClient cliente) {
        this.cliente = cliente;
    }

    /**
     * Envía un mensaje a un canal de Discord y almacena los datos
     * necesarios para permitir posteriores reintentos.
     *
     * @param mensaje contenido del mensaje que se desea enviar
     * @param destinatario canal de Discord al que se dirige el mensaje
     * @return resultado del envío con un intento registrado,
     *         indicando si la publicación fue exitosa
     */
    @Override
    public ResultadoEnvio enviar(String mensaje, String destinatario) {
        this.ultimoMensaje = mensaje;
        this.ultimoCanal = destinatario;
        boolean ok = cliente.publicar(mensaje, destinatario);
        return ok ? ResultadoEnvio.exito(1) : ResultadoEnvio.fallo(1);
    }

    /**
     * Reintenta la publicación del último mensaje enviado hasta alcanzar
     * la cantidad máxima de intentos o conseguir un envío exitoso.
     *
     * @param maxIntentos cantidad máxima de intentos de publicación
     * @return resultado del reintento con la cantidad de intentos realizados
     *         y el estado de éxito o fallo
     */
    @Override
    public ResultadoEnvio reintentar(int maxIntentos) {
        for (int i = 1; i <= maxIntentos; i++) {
            if (cliente.publicar(ultimoMensaje, ultimoCanal)) {
                return ResultadoEnvio.exito(i);
            }
        }
        return ResultadoEnvio.fallo(maxIntentos);
    }
}
