package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.ResultadoEnvio;

/**
 * Implementa el patrón Adapter para integrar {@link FirebaseClient}
 * con el contrato de notificaciones push {@link Notifier}.
 */
public class FirebaseAdapter implements Notifier {

    private final FirebaseClient cliente;
    private String ultimoToken;
    private String ultimoMensaje;

    /**
     * Inicializa el adaptador con el cliente de Firebase.
     *
     * @param cliente cliente encargado de enviar notificaciones push
     */
    public FirebaseAdapter(FirebaseClient cliente) {
        this.cliente = cliente;
    }

    /**
     * Envía una notificación push mediante Firebase y almacena los datos
     * necesarios para permitir posteriores reintentos.
     *
     * @param mensaje contenido de la notificación que se desea enviar
     * @param destinatario token del dispositivo destinatario
     * @return resultado del envío con un intento registrado,
     *         indicando si la notificación fue enviada exitosamente
     */
    @Override
    public ResultadoEnvio enviar(String mensaje, String destinatario) {
        this.ultimoToken = destinatario;
        this.ultimoMensaje = mensaje;
        boolean ok = cliente.enviarPush(destinatario, mensaje);
        return ok ? ResultadoEnvio.exito(1) : ResultadoEnvio.fallo(1);
    }

    /**
     * Reintenta el envío de la última notificación push hasta alcanzar
     * la cantidad máxima de intentos o conseguir un envío exitoso.
     *
     * @param maxIntentos cantidad máxima de intentos de envío
     * @return resultado del reintento con la cantidad de intentos registrada
     *         y el estado de éxito o fallo
     */
    @Override
    public ResultadoEnvio reintentar(int maxIntentos) {
        for (int i = 1; i <= maxIntentos; i++) {
            if (cliente.enviarPush(ultimoToken, ultimoMensaje)) {
                return ResultadoEnvio.exito(i);
            }
        }
        return ResultadoEnvio.fallo(maxIntentos);
    }
}
