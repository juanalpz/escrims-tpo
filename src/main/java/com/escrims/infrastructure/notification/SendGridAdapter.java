package com.escrims.infrastructure.notification;

import com.escrims.domain.entities.ResultadoEnvio;

/**
 * Implementa el patrón Adapter para integrar {@link SendGridClient}
 * con el contrato de notificaciones por correo electrónico {@link Notifier}.
 */
public class SendGridAdapter implements Notifier {

    private final SendGridClient cliente;
    private String ultimoDestinatario;
    private String ultimoMensaje;

    /**
     * Inicializa el adaptador con el cliente de SendGrid.
     *
     * @param cliente cliente encargado de enviar correos electrónicos
     */
    public SendGridAdapter(SendGridClient cliente) {
        this.cliente = cliente;
    }

    /**
     * Envía un correo electrónico mediante SendGrid y almacena los datos
     * necesarios para permitir posteriores reintentos.
     *
     * @param mensaje contenido del correo electrónico que se desea enviar
     * @param destinatario dirección de correo electrónico del destinatario
     * @return resultado del envío con un intento registrado,
     *         indicando si el correo fue enviado exitosamente
     */
    @Override
    public ResultadoEnvio enviar(String mensaje, String destinatario) {
        this.ultimoDestinatario = destinatario;
        this.ultimoMensaje = mensaje;
        boolean ok = cliente.enviarEmail(destinatario, mensaje);
        return ok ? ResultadoEnvio.exito(1) : ResultadoEnvio.fallo(1);
    }

    /**
     * Reintenta el envío del último correo electrónico hasta alcanzar
     * la cantidad máxima de intentos o conseguir un envío exitoso.
     *
     * @param maxIntentos cantidad máxima de intentos de envío
     * @return resultado del reintento con la cantidad de intentos registrada
     *         y el estado de éxito o fallo
     */
    @Override
    public ResultadoEnvio reintentar(int maxIntentos) {
        for (int i = 1; i <= maxIntentos; i++) {
            if (cliente.enviarEmail(ultimoDestinatario, ultimoMensaje)) {
                return ResultadoEnvio.exito(i);
            }
        }
        return ResultadoEnvio.fallo(maxIntentos);
    }
}
