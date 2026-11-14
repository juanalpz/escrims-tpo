package com.escrims.infrastructure.adapter;

import com.escrims.domain.notification.Canal;
import com.escrims.domain.notification.Notificacion;
import com.escrims.infrastructure.http.HttpTransport;

import java.util.Map;

/**
 * «Adapter» Traduce una {@link Notificacion} a un envio de la API Mail Send v3
 * de SendGrid. El destinatario de la notificacion es la direccion de email
 */
public class SendGridAdapter extends HttpNotifierAdapter {

    static final String URL = "https://api.sendgrid.com/v3/mail/send";

    private final String apiKey;
    private final String remitente;

    public SendGridAdapter(HttpTransport transport, String apiKey, String remitente) {
        super(transport);
        this.apiKey = requerido(apiKey, "la API key de SendGrid");
        this.remitente = requerido(remitente, "el email remitente");
    }

    @Override
    public Canal canal() {
        return Canal.EMAIL;
    }

    @Override
    protected String proveedor() {
        return "SendGrid";
    }

    @Override
    protected String url() {
        return URL;
    }

    @Override
    protected Map<String, String> headers() {
        return Map.of("Authorization", "Bearer " + apiKey);
    }

    @Override
    protected String cuerpo(Notificacion n) {
        return "{\"personalizations\":[{\"to\":[{\"email\":" + Json.texto(destinatario(n)) + "}]}],"
                + "\"from\":{\"email\":" + Json.texto(remitente) + "},"
                + "\"subject\":" + Json.texto(n.getTitulo()) + ","
                + "\"content\":[{\"type\":\"text/plain\",\"value\":" + Json.texto(n.getPayload()) + "}]}";
    }
}
