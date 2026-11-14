package com.escrims.infrastructure.adapter;

import com.escrims.domain.notification.Canal;
import com.escrims.domain.notification.Notificacion;
import com.escrims.infrastructure.http.HttpTransport;

import java.util.Map;

/**
 * «Adapter» Traduce una {@link Notificacion} a un mensaje de webhook de
 * Discord. Si la notificacion trae destinatario, se lo menciona en el mensaje
 */
public class DiscordAdapter extends HttpNotifierAdapter {

    private final String webhookUrl;

    public DiscordAdapter(HttpTransport transport, String webhookUrl) {
        super(transport);
        this.webhookUrl = requerido(webhookUrl, "la URL del webhook de Discord");
    }

    @Override
    public Canal canal() {
        return Canal.CHAT;
    }

    @Override
    protected String proveedor() {
        return "Discord";
    }

    @Override
    protected String url() {
        return webhookUrl;
    }

    @Override
    protected Map<String, String> headers() {
        return Map.of();
    }

    @Override
    protected String cuerpo(Notificacion n) {
        String mencion = n.getDestinatario() == null || n.getDestinatario().isBlank()
                ? ""
                : n.getDestinatario() + " ";
        String contenido = mencion + "**" + n.getTitulo() + "**\n" + n.getPayload();
        return "{\"content\":" + Json.texto(contenido) + "}";
    }
}
